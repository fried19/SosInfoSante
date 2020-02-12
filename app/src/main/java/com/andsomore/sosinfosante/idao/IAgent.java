package com.andsomore.sosinfosante.idao;

import android.content.Context;

public interface IAgent<T> {
    public void seConnecter(Context context, T t, IResult result);
}
