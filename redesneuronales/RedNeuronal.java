/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redesneuronales;

import java.util.logging.Level;
import java.util.logging.Logger;
import misUtil.algebraLineal.Agrupador;
import misUtil.algebraLineal.Indice;
import misUtil.algebraLineal.Tensor;

/**
 *
 * @author edriper
 */
public class RedNeuronal {
    CapaNeuronas capas[] ;
    Tensor interfaz[] ;
    Indice k_inter , l_inter, i_inter ;
    String  n_ent = "k" ,
            n_sal = "i" ;
    /*
    * est[] es la estructura de la red neuronal. humm
    */
    public RedNeuronal( int[] est ){
        int numCapas = est.length - 1 ;
        capas = new CapaNeuronas[ numCapas ] ;
        interfaz = new Tensor[ numCapas -1 ] ;
        for(int cCap=0 ; cCap < numCapas ; cCap++)
            capas[cCap] = new CapaNeuronas(est[cCap],est[cCap+1]) ;
        k = new Indice(est[0],"k") ;
        i = new Indice(est[numCapas],"i") ;
        primeraCapa = capas[0] ;
        ultimaCapa = capas[capas.length - 1];
        
    }
    
    
    public Tensor[] datEntre ; //este array tiene dos tensores uno de entrada y otro de salida
    
    // k : indice componente entrada
    // i : indice componente salida
    // l : indice dato entrenamiento
    public Indice k,i,l ;
    
    Tensor tAnt;
    public void pasadaAdelanteDesde( int iCap){
        for( int cCaps = iCap  ; cCaps < capas.length ;  cCaps ++){
                    tAnt = capas[cCaps - 1].darEntradaSig();
                    capas[cCaps].procesarEntrada( tAnt ) ;
            }
    }
    
    
    Tensor salida , error ;
    CapaNeuronas    primeraCapa  ,
                    ultimaCapa   ;
    
    public void retroPropHasta( int iCap){
        
        ultimaCapa.procesarError(error);
        for( int cCaps = capas.length - 2 ; cCaps >= iCap ;  cCaps --){
            tAnt = capas[cCaps+1].darRetroError() ;
            capas[cCaps].procesarError( tAnt ) ;
        }
    }
    
    
    /** 
     * Realiza el entrenamiento de los parámetros del modelo con los parejas de
     * entradas/etiquetas(salida) contenidas en datEnt usando un learning rate igual
     * a paso y hasta tener un gradiente menor o igual que maxNomGrad
     * 
     * @param datEnt Tensor con indices k para numerar las componentes del vector
     * de entrada (el numero de entradas y la dimensión del vector deben coincidir).
     * Además un tensor l para enumerar la cantidad de vectores de entrada en cada
     * lote de entrenamiento
     *
     * @param paso Es un factor que controla la velocidad de aprendizaje, normalmente
     * es una fracción pequeña
     * 
     * @param maxNorGrad determina un factor de terminación del proceso de entrena-
     * miento. Indica el máximo valor de Gradiente admisible para terminar el en--
     * trenamiento
     * 
     * pre: debo saber que los tensores deben tener indices llamados k l e i para
     * las entradas, el lote de entrenamiento y para las salidas respetivamente de
     * hecho k e i son parámetros de la RedNeuronal
     */
    
    public void entrenar( Tensor[] datEnt , float paso , float maxNorGrad ){
        
        //l es indice de número de datos parejas entrada/etiqueta en el lote
        //de entrenamiento
        if(l==null)
            l= (Indice)datEnt[0].darIndice("l").clone() ;
        
        
        CapaNeuronas primeraCapa = capas[0] ,
                     ultimaCapa = capas[capas.length - 1];
        
        
        // i es el indice de la salida (de qué capa de la última? yeap)
        Indice i_error = (Indice)i.clone() ;
        Indice l_error = (Indice)l.clone() ;
        error = new Tensor(new Indice[]{i_error,l_error}) ;
        
        float ng_2 ; //norma del gradiente al cuadrado
        float max_ng_2 = maxNorGrad * maxNorGrad ;
        int cIt = 0 ;
        
        Tensor tAnt;
        
        primeraCapa.procesarEntrada(datEnt[0]);
        pasadaAdelanteDesde(1) ;
        salida = ultimaCapa.darSalida() ; 
        error  = salida.menos(datEnt[1], error);
        float pp ;
        
        do{ //aquí ya debo corregir los pesos de la última capa...
            //capa propagada = capa corregida.
            
            
            /*
            ng_2 = ultimaCapa.darMGP_2() ;
            pp = (float)(Math.sqrt(error.darMagCua() / ng_2 )*paso);
            ultimaCapa.variarPesos(pp);
            
            pasadaAdelanteDesde( capas.length -1 ) ;
            */
           
            for(int cCap=capas.length-1; cCap >= 0 ; cCap--){
                
                retroPropHasta( cCap) ;
                ng_2 = capas[cCap].darMGP_2();
                pp = (float) (Math.sqrt(error.darMagCua() / ng_2) * paso);
                capas[cCap].variarPesos(pp);
                if(cCap>0)
                    pasadaAdelanteDesde(cCap) ;
                else if(cCap==0){
                    primeraCapa.procesarEntrada(datEnt[0]);
                    pasadaAdelanteDesde(1) ;
                }
                salida = ultimaCapa.darSalida() ; 
                error  = salida.menos(datEnt[1], error);
                System.out.println("it: " + (cIt++) + "\t |g|²: "+ ng_2 + "\t |e|²: " + error.darMagCua()) ;
            }
            
            ng_2 = 0 ;
            for( CapaNeuronas cap:capas)
                ng_2 += cap.darMGP_2() ;
            
        } while(( ng_2 > max_ng_2 ) && (error.darMagCua() > 0.1)) ;
        
        
    }
    
    
    
    
    public static void main(String[] args){
        /*  Tratemos de crear un modelo que separe una región anular.
            los datos de entrenamiento serán un grupo 300 datos, tomados
            aleatoriamente de un cuadrado de 20 x 20 , siendo la salida
            esperada un 1 si el radio al centr  o del cuadrado está entre
            3 y 4 unidades.
        */
        
        /******************
          Creando los datos de entrenamiento.
        *********************/
        float[][] datos = new float[300][3] ;
        //cada dato de entrenamiento tiene dos coordenadas del plano y la pertenencia o
        //no de ese punto a la región anular 3 < r < 4.
        
        java.util.Random azar = new java.util.Random() ;
        float r_2 , x , y;
        
        int nUnos = 0 ;
        
        //datos dentro del anillo (no había necesitad de dos indices
        for(int cDat=0 ; nUnos < 100 ; ){
            datos[cDat][0] = x =(float)((azar.nextFloat()  - 0.5 ) * 20) ;
            datos[cDat][1] = y =(float)((azar.nextFloat()  - 0.5 ) * 20) ;
            r_2 = x*x + y*y ;
            if((9 < r_2)&&(r_2 < 16)){
                datos[cDat++][2]= 1 ;
                nUnos++ ;
            } 
        }
        
        
        //datos en la parte exterior del anillo r > 5
        int nCer20=0 ;
        for(int cDat=100 ; cDat < 200 ; ){
            datos[cDat][0] = x =(float)((azar.nextFloat()  - 0.5 ) * 30) ;
            datos[cDat][1] = y =(float)((azar.nextFloat()  - 0.5 ) * 30) ;
            r_2 = x*x + y*y ;
            if(20 < r_2) {
                datos[cDat++][2]= 0.01f ;
                nCer20++ ;
            } 
        }
        
        //datos en el interior de la region anular r< 2
        int nCer0=0;
        for(int cDat=200 ; cDat < 300 ; ){
            datos[cDat][0] = x =(float)((azar.nextFloat()  - 0.5 ) * 4) ;
            datos[cDat][1] = y =(float)((azar.nextFloat()  - 0.5 ) * 4) ;
            r_2 = x*x + y*y ;
            if(((r_2 < 4) ))
                datos[cDat++][2]= 0.01f ;
        }
        
        
        System.out.println("número de unos: " + nUnos) ;
        
        
        /* Un tensor es un 'arreglo' n-dimensional de datos. Datos que son
           accesibles mediante un n-upla de indices.
        */
        
        //creamos los tensores
        Tensor x_kl , y_il , datEnt[] ;
        Indice k,i,l_x,l_y ;
        k= new Indice(2,"k") ; //k es el indice de componentes de la entrada: rango de var 2
        i= new Indice(1,"i") ; //i es el indice de componentes de la salida: rango de var 1
        l_x= new Indice(datos.length,"l") ; //l es el indice del numero de datos de entre: rango de var 300
        l_y= (Indice)l_x.clone() ;
        
        x_kl = new Tensor(new Indice[]{k,l_x}) ;
        y_il = new Tensor(new Indice[]{i,l_y}) ;
    
        //dado que se necesita, debería haber una función que toma un conjunto de
        //indices y regresa otro indice que hace variar a los indices componentes
        //al mismo tiempo.
        Agrupador l_es = new Agrupador( new Indice[] {l_x,l_y}) ;
        boolean desbor_l = false ,
                desbor_k  ;
        i.reiniciar() ; 
        for(l_es.reiniciar();!desbor_l;desbor_l=l_es.inc()){
            y_il.fijarCompAct( datos[l_y.estado][2] ) ;
            for( k.reiniciar() , desbor_k = false; !desbor_k ; desbor_k = k.inc() )
                x_kl.fijarCompAct( datos[l_x.estado][k.estado] ) ;
        }
        //es un array de Tensores con dos componentes las entradas x y 
        //las salidas y 
        datEnt = new Tensor[]{x_kl,y_il};
        
        /*  ahora creamos la RedNeuronal
            con tres capas, la primera tiene 2 entradas y 6 neuronas (salidas), 
            la segunda tendra 6 entradas y 5 neuronas(salidas) y la
            última debe tener 5 entradas y una neurona o una salida 
        
            la estructura es {2,20,20,1}
        */
        RedNeuronal modelo = new RedNeuronal(new int[] {2,10,8,2,1} );
        //ahora entrenamos el modelo
        modelo.entrenar(datEnt, 6e-3f, 50e-4f);
    }    
}
