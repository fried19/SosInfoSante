package com.andsomore.sosinfosante.idao;

import com.andsomore.sosinfosante.entite.Incident;

public interface IResponse {
    public void getResult(int val);

    public void getResult(boolean ok,String code);

}
