/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package misUtil.algebraLineal;

/**
 * public class Expansor. esta abstracción, permite crear de manera simple y única los
 * ciclos que puedan aparecer en las operaciones con tensores (multiplicación matricial). 
 * permite anidar ciclos (de suma en la contracción) dentro de otros ciclos (cálculo de
 * una componente del Tensor). Para ello se dota a este Contador con su principal
 * característica cual es la de poder interrumpir el proceso de incremento de los
 * contadores (externos). Qué pasa cuando se interrumpe el ciclo???
 * @author edriper
 */
public class Expansor
    extends Contador{
    
    Contador[] contadores ;
    public Expansor(Contador c[]) {
        contadores = c ;
    }
        
    int cInd ,      //contador de Indice
        indInt ;    //indice de Interrupcion
    boolean completo = true ; //último incremento está completo
    boolean proxVarExt = false ; //proximo inc varia el estado de los Cont Exter
    
    @Override
    public void reiniciar(){
        for (Contador contadorX : contadores) 
            contadorX.reiniciar();
        /*
            Los contadores de un Extensor son agrupados en internos y "externos"
        o NO internos. Un incremento puede variar el estado de uno o varios con-
        tadores del Extensor, pero a efectos de lograr el comportamiento deseado
        un incremento se dividirá para que solo modifique el estado de los conta-
        dores internos o de los externos pero no ambos. De modo que ANTES de mo-
        dificar el valor del estado de los contadores externos el incremento debe
        haber sido "interrumpido". es decir completo debe ser FALSE.
        
        Luego si no hay si nó hay sino contadores externos, entonces todos y cada
        uno de los incrementos serán como los realizados cuando se ha dividido
        el incremento.
        
        */
        proxVarExt =  !contadores[0].esInterno ;
    }
    
    /**
     * pubic boolean inc(). Incrementa el valor del conjunto de contadores
     * en una unidad. Esto implica que pueda ser necesario incrementar varios
     * de los grupos de contadores que componen al Expansor.
     * Un desborde ocurre cuando se ha pasado por todas las combinaciones posibles
     * de estados individuales de cada grupo y se hace un incremento.
     * 
     * Por regla los contadores internos (aquellos que se van a "contraer") se
     * incrementan primero
     * 
     * @return true si ocurre un desborde, false en cualquier otro caso
     */
    @Override
    public boolean inc( )
    {   /*completo true indica:
        *   Que en último incremento, no se debía variar el estado de los 
        *   contadores NO internos y por tanto el anterior incremento se encuenra
        *   completo
        * completo false indica:
        *   Que el último incremento no se pudo realizar por completo pues para
        *   llevarlo a cabo se debía incrementar uno o varios de los contadores internos
        *   y uno de los NO internos
        */
    
        if( completo )
            cInd = 0 ; //solo ocurre una vez por cada componente del resultado
        else 
            completo = true ; //el incremento se completa en este incremento
        
        proxVarExt =  !contadores[0].esInterno ;
        
        boolean pulso ;
        Contador contAct , //cont Actual
                 sigCont ; //siguient contador
        int nC = contadores.length; //numero de contadores 
        
        for( pulso = true ; (cInd < nC) && pulso ; cInd++){
                pulso = (contAct = contadores[cInd]).inc() ;
                
                if( contAct.esInterno && 
                    pulso && 
                    (cInd+1)< nC ){
                    sigCont = contadores[cInd+1] ;
                    if(!sigCont.esInterno){
                        completo = false ;
                        pulso = false ;
                        proxVarExt =  true ;
                        cInd++ ;
                        break  ;
                    }
                }
        }
        return pulso ;
    }
    
    
    
    public static void main( String[] args){
        Indice a,b,c ;
        a = new Indice(4,"i") ;
        b = new Indice(5,"j") ;
        c = new Indice(6,"k") ;
        
        a.esInterno = true ;
        Contador ctdrs[] = {a,b,c} ;
        b.esInterno = true ;
        
        Expansor ex = new Expansor(ctdrs) ;
        
        boolean desb ;
        //aca se definiría un acumulador y se inicia a cero
        for( desb =  false , ex.reiniciar() ; !desb ; desb=ex.inc()){
                
            if(ex.completo)
                System.out.println( "se ACUMULA la mult de las comp con indices \n" +
                    "indice a: " + a.estado() + " " +
                    "indice b: " + b.estado() + " " +
                    "indice c: " + c.estado() + " ");

            if(ex.proxVarExt){

                System.out.println( "se ASIGNA el acum a la componente \n"
                          + "indice a: " + a.estado() + " " + 
                            "indice b: " + b.estado() + " " +
                            "indice c: " + c.estado() + " ");

            }
        }
        
        System.out.println( "finalmente indice a: " + a.estado() + " " + 
                                "indice b: " + b.estado() + " " +
                                "indice c: " + c.estado() + " ");
        
    }
}
