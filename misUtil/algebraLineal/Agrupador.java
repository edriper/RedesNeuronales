/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package misUtil.algebraLineal;

/**
 *
 * @author edriper
 */
public class Agrupador 
    extends Contador{
    Indice contadores[] ;
    
    public Agrupador(Indice a[]){
        contadores = a ;
        if (a.length>1){
            int dim = a[0].max ;
            for(int i=1 ; i < a.length ; i++)
                if (a[i].max != dim)
                    //System.out.println("los Indices no tienen el mismo tamaño");
                    ;
        } else
            System.out.println("no hemos pensado en contraer menos de dos indice");
            
    }
    
    public Agrupador(Indice a[], boolean interno){
        this(a);
        esInterno=interno;
    }
    
     /**
     * pubic boolean inc(). Incrementa el valor del estado en una unidad
     * si alcanza el valor max, el estado retorna a cero y la función retorna
     * verdadero (indicando que hubo desborde)
     * @return true si ocurre un desborde, false en cualquier otro caso
     */
    @Override
    public boolean inc() {
        boolean desbor = false ;
        for (Indice contadore : contadores) {
            desbor = contadore.inc() || desbor;
            if(desbor)
                reiniciar() ;
        }
        
        return desbor ;
    }
    
    /**
     * pues eso reinicia los contadores agrupados
     */
    
    public void reiniciar(){
        for (Indice contadore : contadores) 
            contadore.reiniciar();
    }
    
    public int estado(){
        return contadores[0].estado ;
    }
    
    public static void main(String[] args){
        Indice a,b,c ;
        a = new Indice(4,"i") ;
        b = new Indice(4,"j") ;
        c = new Indice(6,"k") ;
        
        Indice indices[] = {a,b} ;
        Agrupador d = new Agrupador(indices) ;
        d.esInterno = true ;
        //primero se ponen los indices contraibles
        Contador ctdrs[] = {d,c} ;
        
        Expansor ex = new Expansor(ctdrs) ;
        
        //aca de debo iniciar el ciclo interno
        boolean desb ;
        for (desb = false, ex.reiniciar(); !desb; desb = ex.inc()) {

            if (ex.completo) {
                System.out.println("se ACUMULA la mult de las comp con indices \n"
                        + "indice a: " + a.estado() + " "
                        + "indice b: " + b.estado() + " "
                        + "indice c: " + c.estado() + " ");
            }

            if (ex.proxVarExt) {

                System.out.println("se ASIGNA el acum a la componente \n"
                        + "indice a: " + a.estado() + " "
                        + "indice b: " + b.estado() + " "
                        + "indice c: " + c.estado() + " ");

            } 

        }
    }
}
