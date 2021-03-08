package cn.analysys.casedemo.utils.id;

public interface IdCaller {
    public void SeeUid(int uid);

    public void SeeUid(int uid, String app, String pkg);

    public void SeePid(int pid);
}
