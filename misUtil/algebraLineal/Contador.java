/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package misUtil.algebraLineal;

/**
 * public class Contador. Esta clase es un contador de cero a un valor max
 * se puede incrementar y si sobrepasa la cuenta max se reinicia
 * @author edriper
 */
public abstract class Contador{
    
    
    /**
     * pubic boolean inc(). Incrementa el valor del estado en una unidad
     * si alcanza el valor max, el estado retorna a cero y la función retorna
     * verdadero (indicando que hubo desborde)
     * @return true si ocurre un desborde, false en cualquier otro caso
     * 
     */
    public abstract boolean inc() ;
    public abstract void reiniciar() ;
    
    public boolean esInterno ;
           
}
