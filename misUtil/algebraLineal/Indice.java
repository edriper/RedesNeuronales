/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package misUtil.algebraLineal;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author edriper
 */
public class Indice
    extends Contador    
    implements Cloneable{
    public int max ;
    public int estado ;
    public boolean marca ;
    public String nombre ;
    
    @Override
    public void reiniciar(){
        estado = 0 ;
    }
    
    public Indice( int max, String t){
        this.max = max ;
        estado = 0 ;
        nombre = t ;
    }
    
    @Override
    public boolean inc(){
        if((++estado % max ) == 0){
            estado = 0 ;
            return true ;
        }
        return false ;
    }
    
    public int estado(){
        return estado ;
    }
    
    public void ponerA(int est){
        estado = est ;
    }
    
    @Override
    public Object clone() {
        try {
            return super.clone() ;
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Indice.class.getName()).log(Level.SEVERE, null, ex);
            return null ;
        }
    }
}
