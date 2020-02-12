package com.andsomore.sosinfosante.idao;

import android.content.Context;

public interface IIncident <T>{
    public void creerIncident(Context context, T t, IResult iResult);
}
