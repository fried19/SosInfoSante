package com.andsomore.sosinfosante.idao;

import android.content.Context;

public interface IUtilisateur<T> {
    public void seConnecter(Context context, T t, IResult result);
    public void seDeConnecter(T t, IResult result);
    public void modifierInfoCompte(Context context,T t, IResult result);
    public void supprimerCompte(T t);
    public void creerCompte(T t, IResult result);
    void isSecouriste(Context context, String email, String password, IResult result);
}
