package me.hhhaiai;

import java.io.Serializable;

public interface ImpTask extends Serializable {
    public abstract String getName();

    public abstract void work();
}
