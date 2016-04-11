/*package connection;
/** Metodo per calcolare il ping effettivo in millisecondi. Sull'emulatore funziona solo
 * su 127.0.0.1
 * @param ip Indirizzo IP dell'host da pingare
 * @return Il numero di millisecondi trascorsi oppure Long.MAX_VALUE altrimenti.
 */
/*public long ftpPing2(String ip) {//qui da qualche parte glielo devo inserire, nella pagina verifica
    try {
        long start = System.currentTimeMillis();
        Process p = Runtime.getRuntime().exec("ping -c 1 " + ip);
        p.waitFor();
        int exit = p.exitValue();
        if (exit == 0)
            return System.currentTimeMillis() - start; //Ritorna il tempo in millisecondi
        Log.i(APPNAME, "PING EXIT CODE: ", + exit+ "");
    } catch (IOException e1) {
        e1.printStackTrace();
        Log.d(APPNAME, "Errore in exec ping");
    }catch(InterruptedException e2) {
        e2.printStackTrace();
        Log.d(APPNAME, "Errore in Process waitFor");
}
    return Long.MAX_VALUE;
}
}*/

