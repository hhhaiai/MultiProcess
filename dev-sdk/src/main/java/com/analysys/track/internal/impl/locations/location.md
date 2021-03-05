# 定位需要权限和说明

## 获取最近一次位置

``` java
LocationManager.getLastKnownLocation 
```

* 需要权限 

```
anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}
```

## 获取基站信息

``` java
TelephonyManager.getAllCellInfo() 
```

* 需要权限 

```
android.Manifest.permission.ACCESS_FINE_LOCATION
```