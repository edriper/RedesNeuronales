/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package misUtil.algebraLineal;

/**
 * public class Tensor. Un tensor es un vector. Cuyas componentes se organizan en
 * un arreglo multidimensional, al que se puede acceder por medio de varios índices.
 * 
 * Este hecho de ordenar las componentes de un vector en un arreglo multidimensional
 * ha demostrado ser muy útil. Pero también hace confuso el hablar de dimensiones.
 * puesto que por un lado están las dimensiones del espacio vectorial al que pertenece
 * el Tensor (igual al número total de componentes) pero de otra parte están las
 * dimensiones en las que se arreglan sus componentes.
 * 
 * Debido a la forma en que se arreglan las componentes del Tensor se requieren
 * vario indices, uno por cada dimensión(del arrglo de componentes). Cada Indice 
 * tiene un extensión (rango de variación), lo cual permite determinar el "tamaño" 
 * del tensor (es decir la dimensión del espacio vectorial al que pertenece el Tensor).
 * 
 * al número de indices (dimensión del arreglo de componentes) se le conoce como
 * orden del Tensor.
 * 
 * Aparte de la suma (la misma operación del espacio vectorial), existe un 
 * producto tensorial y la contracción de indices que permiten obtener Tensores
 * de diferente "naturaleza" (de diferente dimension u orden)
 * 
 * @author edriper
 */
public class Tensor {
    Indice ies[] ;  // arreglo de índices
    float  elemento[] ; //componentes del tensor
    int marcas[] ; // las marcas simplifican los cambios de indices a componente

    public Tensor( Indice i[]){
        /* tal como está con la variable i se tendría acceso a los indices
           pero quizá no sea tan malo
        */
        ies = i ;
        //estas marcas son usadas para organizar el conjunto de componentes
        //en el arreglo deseado y así poder acceder a una componente determinada
        //a partir del conjunto de valores de su indice.
        //Se puede ver como cada nuevo indice crea un array de Tensores
        //de orden inmediatamente anterior.
        marcas = new int[i.length] ;
        int tdim ;
        int tam = 1;
        for(int cInd=0 ; cInd < i.length ; cInd++){
            tdim = i[cInd].max ;
            marcas[cInd] = tam ;
            tam *= tdim ;
        }
        
        elemento = new float[tam]  ;        
    }
    
    /**
     * Retorna el indice a la componente del Tensor (como vector) correspondiente
     * al conjunto de valores de sus indices.
     * 
     * @return 
     */
    protected int iActual(){
        int ind = 0 ;
        for(int cInd=0 ; cInd < ies.length ; cInd++)
            ind += marcas[cInd] * ies[cInd].estado ;
        return ind ;
    }
    
    
    /**
     * Retorna el valor de la componente del Tensor (como vector) correspondiente
     * al conjunto de valores de sus indices.
     * 
     * @return 
     */
    public float compAct(){
        return elemento[iActual()] ;
    }
    
    
    /**
     * Retorna el valor de la componente del Tensor (como vector) correspondiente
     * al conjunto de valores los indices pasados como parámetros.
     * 
     * obviamente el número de indices y el valor de estos debe ser afín al número
     * y rango de los indices del Tensor
     * 
     * @return 
     */
    public float comp( int i_es[]){
        int ind = 0 ;
        for(int cInd=0 ; cInd < i_es.length ; cInd++)
            if(i_es[cInd]<ies[cInd].max){
                ind = marcas[cInd] * i_es[cInd] ;
                ies[cInd].estado = i_es[cInd] ;
            }
            else return Float.NaN ;
        return elemento[ind] ;
    }
    
    
    
    public void fijarCompAct( float c){
        elemento[iActual()] = c ;
    }
    
    boolean mismaNatu( Tensor b){
        if (marcas.length != b.marcas.length)
            return false ;
        boolean ret = true ;
        for(int cInd=1 ; ( cInd < marcas.length ) && ret  ; cInd++)
            ret = ret && (marcas[cInd] == b.marcas[cInd]) ;
        ret = ret && (elemento.length == b.elemento.length) ;
        return ret ;
    }
    
    public Tensor mas( Tensor b, Tensor r){
        if( mismaNatu(b) && mismaNatu(r)){
            for(int cEl= 0 ; cEl < elemento.length ; cEl++)
                r.elemento[cEl] = elemento[cEl] + b.elemento[cEl] ;
            return r ;
        }else
            return null ;
    }
    
    public Tensor menos( Tensor b, Tensor r){
        if( mismaNatu(b) && mismaNatu(r)){
            for(int cEl= 0 ; cEl < elemento.length ; cEl++)
                r.elemento[cEl] = elemento[cEl] - b.elemento[cEl] ;
            return r ;
        }else
            return null ;
    }
    
    public void desMarcarIndices(){
        for(int cInd=0 ; cInd < ies.length ; cInd++)
            ies[cInd].marca = false ;
    }
    
    public Indice[] darIndices(){
        return ies ;
    }
    
    //Contadores por expandir, seguro lo hago estatico para ahorrar memoria,
    //se usa dentro de la función para crear el expansor que controla el
    //ciclo de multiplicación contracta
    static protected Contador porExp[] = new Contador[40] ;
    public Tensor por( Tensor b[] , Tensor r , Indice[][] GIContraer, Indice[][] GISalida){
        
        //primero contamos con el espacio de los contractores
        int cuantos = GIContraer.length ;
        cuantos += GISalida.length ;
        
        //ahora agregemos espacio para el resto de los indices a expandir
        //desmarcamos los indices de todos los tensores
        desMarcarIndices() ;
        for (Tensor b1 : b) {
            b1.desMarcarIndices();
        }
        
        //marcamos los indices que se van a contraer
        for (Indice[] contraer1 : GIContraer) 
            for (Indice contraer11 : contraer1) 
                contraer11.marca = true;
            
        
        
        //marcamos los indices que deben variar como uno solo
        for (Indice[] unido : GISalida) 
            for (Indice unido1 : unido) 
                unido1.marca = true;
            
        
        //creo que es pérdida de tiempo... pero fijemos qué hace
        int cIndSinMarca = 0 ;
        for(int cInd=0 ; cInd < ies.length ; cInd++)
            if(!ies[cInd].marca)
                porExp[cIndSinMarca++] = ies[cInd] ;
            
        
        for(int cTen=0 ; cTen < b.length ; cTen++)
        for(int cInd=0 ; cInd < b[cTen].ies.length ; cInd++)
            if(!b[cTen].ies[cInd].marca)
                porExp[cIndSinMarca++] = b[cTen].ies[cInd]  ;
        
        
        //creamos los contadores
        Contador ctdores[] = new Contador[cuantos+cIndSinMarca] ;
        
        int llevo ;
        //se crea un nuevo agrupador por cada array de indices a contraer.
        for( llevo=0 ; llevo < GIContraer.length ; llevo++ ){
            ctdores[llevo] = new Agrupador(GIContraer[llevo]) ; 
            ctdores[llevo].esInterno = true ;
        }
        
        //se crea un nuevo agrupador por cada array de indices de la salida
        int antesLlevaba = llevo ;
        for(llevo=0 ; llevo < GISalida.length ; llevo++)
            ctdores[antesLlevaba+llevo] = new Agrupador( GISalida[llevo]) ;
        
        //creí que solo habrían indices de lo primeros grupos.
        // !entre mas lo pienso mas me convenzo que NO deben existir estos so pena de error!
        antesLlevaba += llevo ;
        for(llevo=0 ; llevo < cIndSinMarca ; llevo++)
            ctdores[antesLlevaba+llevo] = porExp[llevo] ;
        
        //ahora podemos hacer el expansor
        Expansor ex = new Expansor( ctdores ) ;
            
        //int interrup = GIContraer.length - 1 ;
        
         
        float   suma = 0 ,
                prod = 1 ;
        //ex.cuentaInt = false ;
        //ex.reiniciar() ;
        
        //for(boolean desb=false; !desb ; ){
        boolean desb ;
        for(desb =  false , ex.reiniciar() ; !desb ; desb=ex.inc()){
            
                if(ex.completo){
                     //acá va el ciclo interno
                    prod = 1 ;
                    for (Tensor b1 : b) 
                        prod *= b1.compAct();
                    
                    suma += (prod * compAct()) ;
                } 
                
                if(ex.proxVarExt){
                    r.fijarCompAct(suma);
                    suma = 0 ;
                }
           
        }
        // falta si el producto contracto es un escalar
        return r ;
    }
    
    public Indice darIndice(String n){
        for(int cInd=0 ; cInd<ies.length ; cInd++)
            if(ies[cInd].nombre.equalsIgnoreCase(n))
                return ies[cInd] ;
        return null ;
    }
    
    public float darMagCua(){
        float ret = 0;
        for(int cEle=0 ; cEle < elemento.length ; cEle++)
            ret += elemento[cEle] * elemento[cEle] ;
        return ret ;
    }
    
    public static void main(String[] args) {
        Indice indEnt, indSal ;
        
        indEnt = new Indice(5,"k") ;
        indSal = new Indice(3,"i") ;
        
        
        //se crean los tensores
        Tensor tEnt,tSal,tPesos ;
        
        Indice ies_ent[] = {(Indice)indEnt.clone()} ,
               ies_Pesos[],
               ies_Sal[] ; 
        tEnt = new Tensor(ies_ent) ;
        
        ies_Pesos = new Indice[]{ (Indice)indEnt.clone() ,
                            (Indice)indSal.clone() } ;
        tPesos = new Tensor(ies_Pesos) ;
        
        ies_Sal = new Indice[]{ (Indice) indSal.clone() } ;
        tSal = new Tensor(ies_Sal) ;
        
        
        
        
        //estos serían los valores de las componentes de los tensores
        float pesos[][] = { {5, -1, 4} ,
                            {1, 0 , 2} ,
                            {-2, 3, 0} ,
                            {1,  2, 3} ,
                            {4, -5, 1} } ,
              ent[] = {-2, 0, 5, 1, -1}; // el producto da {-23,24,-6}
        
        Agrupador k = new Agrupador(new Indice[]{ tPesos.darIndice("k"),
                                                    tEnt.darIndice("k")   }) ;
        Indice i = tPesos.darIndice("i") ;
        
        boolean desborK = false ,
                desborI = false ;
        //aquí se ponen los valores iniciales de los pesos y la entrada                
        for(k.reiniciar(); !desborK ; desborK=k.inc() ){
            tEnt.fijarCompAct(ent[k.estado()]);
            desborI = false ;
            for( i.reiniciar(); !desborI ; desborI=i.inc() )
                tPesos.fijarCompAct(pesos[k.estado()][i.estado()]);
        }
        
        //aquí se prepara todo para la multiplicación w_ki otimes ent_k
        Tensor b[] = { tPesos } ;
        Indice contraer[][] = { {   tPesos.darIndice("k") ,
                                    tEnt.darIndice("k")   }  };
        
        Indice unidos[][] = { { tSal.darIndice("i") ,
                                tPesos.darIndice("i") } } ;
        
        tSal = tEnt.por(b, tSal, contraer, unidos) ;
        System.out.println("fin") ;
        
    }
    
}
