package com.andsomore.sosinfosante.idao;

import android.content.Context;

public interface IIntervention <T> {
    public void demarrerIntervention(T t, IResult iResult);
}
