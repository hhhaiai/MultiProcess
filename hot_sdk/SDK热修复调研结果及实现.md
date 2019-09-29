---
title: SDK热修复调研结果及实现
date: 2019-09-29 12:37:20
tags:
---

## 背景

对SDK实现热修复,在客户出问题或者面临新版本Android兼容性问题的时候,我们可以在用户不用动任何代码的情况下,实现对代码的更改,因此如果能实现一个稳定可用的热修复,同时对目前的项目影响不大的情况下,是非常有利的.

## 需求梳理

下面是我通过对SDK的了解,梳理的一个需求,这个需求可能还比较片面,后续可以继续补充.  

热修复在实现以后,应该做到如下保证:

1. 保证原来的代码逻辑完全不变,并正常运行.
2. 保证客户的对接方式,跟目前一致.
3. 保证旧客户接入的SDK仍然可用,即,向后兼容
4. 客户是需要知情SDK热修复能力的,因此对不信任热修复的客户,可以支持非热修复版本,并且,非热修复版本和有热修复版本调用方式,逻辑,都是一致的,代码也是重用的.

## 实现调研

### 第一种方式

调研了shadow 热修复框架的实现方式,shadow 号称无反射插件化框架,实际上作者也承认这个说法是不准确的,因为它至少有一处反射,那就是反射了Android ClassLoader的 private final 域的mParent,然后通过反射在原来的 BaseClassLoader 和 PathClassLoader 中间 插入了自定义的DexClassLoader,利用"被误导的"双亲委托机制,来实现了插件化.以下引用一下作者对这块分析的原文:

> Android系统的虚拟机和一般的JVM有一点不太一样，就是可以通过反射修改private final域。这在一般的JVM上是不能成功的，读过《Java编程思想》的同学可能还记得专门有这段讲解。而ClassLoader类的parent域，恰恰就是private final域。ClassLoader的parent指向的是ClassLoader的“双亲”，就是“双亲委派”中的那个“双亲”（现在去学习这个概念的同学注意这里的“双”是没有意义的，不存在两个“亲”）。宿主的PathClassLoader就是一个有正常“双亲委派”逻辑的ClassLoader，它加载任何类之前都会委托自己的parent先去加载这个类。如果parent能够加载到，自己就不会加载了。因此，我们可以通过修改ClassLoader的parent，为ClassLoader新增一个parent。将原本的BootClassLoader <- PathClassLoader结构变为BootClassLoader <- DexClassLoader <- PathClassLoader，插入的DexClassLoader加载了ContainerActivity就可以使得系统在向PathClassLoader查找ContainerActivity时能够正确找到实现。  
> 作者：shifujun    
> 链接：https://juejin.im/post/5d1b466f6fb9a07ed524b995    
> 来源：掘金    
> 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。 

这里我其实还有个地方没明白,ClassLoader是有缓存的,如果有缓存,则直接返回,就不会继续往上访问mParent了,而Android 在开始运行时 ? 是一次把所有的类都加载进来了呢?还是运行到哪里就加载到哪里呢? 这还跟不同的虚拟机实现有关,java的虚拟机就是运行到哪里就加载到哪里,但Android的貌似不是这样,这一块我没有验证.

*TODO:验证计划是这样的:拷贝BaseDexClassLoader 源码到项目中,然后运行Debug调试看看是怎么走的,还不确定这个验证行不行得通. *

不过不管Android class 是在启动的时候全部加载,还是运行中加载, 只要宿主app中没有,那么必定是用的时候再加载,因此可以先写一个壳子,然后这个壳子负责热修复包的更新和应用的管理.并判断是否可用,如果可用,则激活使用.

而热修复包就是现在项目完全不动,打一个jar包就可以了,并且这个项目可以单独不依赖于热修复壳子来使用.满足需求4,单独使用的目的.

### 第二种方式

第二种方式自定义ClassLoader 并禁用双亲委托 ,通过反射创建对象并赋值给接口,因为java向上转型是安全的,而同类型转型是不安全的.  
打个比方,AImpl实现了A的接口,AImpl.class 由两个不同的ClassLoader加载,则此时使用1号ClassLoader创建的 AImpl的对象无法转换为2号ClassLoader加载的AImpl.class,报出类转换异常.但不管哪个类加载器加载出来的AImpl.class,对A转型始终是安全的.

因此利用这个规则,我们可以实现一些A的子类并且加载,达到我们的目的

跟第一个方式一样,壳子和热修复包的设计,是一致的,只是壳子实现的方式不同.

## 实现壳子

第一种方法反射最少,不会发生多类加载器交叉冲突的问题,侵入性最低,而且改动比较简单,因此想尝试以第一种方式来实现,分为以下步骤实现:

1. 实现Hack private final parent;
2. 实现更新修复包逻辑
3. 实现修复包应用逻辑
4. 实现修复包卸载,替换,回滚等逻辑

### 实现Hack private final parent;

hack的目的 是更改项目中原来的双亲委派结构.如下示意:

hack前:BootClassLoader-PathClassLoader   
-----------------------↓------------------------  
hack后:BootClassLoader-RuntimeClassLoader-PathClassLoader

其中,RuntimeClassLoader则是我们加载我们的热修复包,主要要用的.

代码:(一些片段来自开源shadow的一部分)

```java
import java.lang.reflect.Field;

public class HackClassLoader {
    public static void hackParentClassLoader(ClassLoader classLoader,
                                              ClassLoader newParentClassLoader) throws Exception {
        Field field = getParentField();
        if (field == null) {
            throw new RuntimeException("在ClassLoader.class中没找到类型为ClassLoader的parent域");
        }
        field.setAccessible(true);
        field.set(classLoader, newParentClassLoader);
    }

    /**
     * 安全地获取到ClassLoader类的parent域
     *
     * @return ClassLoader类的parent域.或不能通过反射访问该域时返回null.
     */
    private static Field getParentField() {
        ClassLoader classLoader = HackClassLoader.class.getClassLoader();
        ClassLoader parent = classLoader.getParent();
        Field field = null;
        for (Field f : ClassLoader.class.getDeclaredFields()) {
            try {
                boolean accessible = f.isAccessible();
                f.setAccessible(true);
                Object o = f.get(classLoader);
                f.setAccessible(accessible);
                if (o == parent) {
                    field = f;
                    break;
                }
            } catch (IllegalAccessException ignore) {
            }
        }
        return field;
    }
}

```

### 实现更新修复包逻辑 //todo
### 实现修复包应用逻辑 //todo
### 实现修复包卸载,替换,回滚等逻辑 //todo

## 目前想到的一些可能风险

对于各种类型的 private static final 常量这种,以及各种内部类的加载应该验证一下值是不是正确

下发下去的修复包安全考虑加密,并且加载之前需要验证

加载 assert 中的 jar有的市场上线可能会对apk进行扫描报毒,可以试试发apk或者对jar加密应对