/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redesneuronales;

import java.util.Random;
import javax.swing.JPanel; //para qué?
import misUtil.algebraLineal.Agrupador;
import misUtil.algebraLineal.Indice;
import misUtil.algebraLineal.Tensor;

/*
 * esta es una capa densa con función de activación logistica
 * @author edriper
 */
public class CapaNeuronas {
    /*
    *  erp_ant_il:  error retro prop capa ant - indice i,l
    *  x_sig_kl:    entrada x capa sig_ ind k,l
    *  w_ki:    parametros - indice k-i
    *  y_il:    salida - indice i,l
    *  dy_il:   derivada salida respecto variacion suma pond
    *  z_il:    suma ponderada
    *  e_il:    gradiente fun coste respecto salidas y
    *  g_ki:    gradiente fun coste respecto parametros w
    *  erp_kl:  gradiente fun coste respecto entradas x
    *
    */
    public Tensor erp_ant_il, x_sig_kl ;
    Tensor x_kl, w_ki, y_il , dy_il , z_il , e_il , g_ki , erp_kl ;
    Indice i,k,kex,l, k_prox , i_ant ;
    
    Random azar = new Random() ; //para inicializarla de forma aleatoria
    
    //constructor
    public CapaNeuronas( int nEnt , int nSal){
        i = new Indice( nSal , "i") ; 
        k = new Indice( nEnt , "k") ;  
        kex = new Indice (nEnt+1,"k") ; // k extendido el +1 por el bias
        w_ki = new Tensor(new Indice[]{ (Indice)kex.clone() ,
                                        (Indice)i.clone() } )  ;
        
        Indice  k_w = w_ki.darIndice("k") ,
                i_w = w_ki.darIndice("i");
        
        //ToDo el tensor podría dar el contador Expansor que permita recorrer todas
        //sus componentes.
        boolean desbor_i , desbor_k ;
        for( k_w.reiniciar(),desbor_k=false ; !desbor_k ; desbor_k = k_w.inc())
            for( i_w.reiniciar(),desbor_i=false ; !desbor_i ; desbor_i = i_w.inc())
                w_ki.fijarCompAct((float)(azar.nextFloat()*0.0002-0.0001));
        
    }
    
    /* prediccionLote -- llamada en la pasada hacia adelante
    *   completa x_kl a partir de ent_kl agregandole una dim para el byas con valor 1
    *   calcula las suma ponderada z_il
    *   calcula las salidas y_il
    *   calcula las derivadas dy_il de las salidas
    */
    public void procesarEntrada( Tensor ent_kl ){
        
        //copiamos ent_kl en x_kl poniendo el 1 del bias como última componente
        //de entrada.
        /* el comportamiento debería ser que se modifique el tamaño de los tensores
        *  que tengan el indice l, solo cuando no quepan los datos. Adicionalmente
        *  debe modificarse el máximo del indice.
        *
        */
        Indice  l_ent = ent_kl.darIndice("l") ,
                k_ent = ent_kl.darIndice("k") ;
        
        if((l == null) || (l.max != l_ent.max))
            l = (Indice)l_ent.clone() ;
         
        
        //Nos preparamos para copiar las salidas de la capa anterior
        //en las entradas de esta capa. Se deben "sincronizar" la variación
        //de los indices: consigue los indices
        Indice l_x , k_x ;
                
        if(x_kl == null){
            l_x = (Indice)l.clone() ;   //l es una muestra de todo la capa?? este si debe variar
            k_x = (Indice)kex.clone() ; //kex es la muestra del numero de entradas esto no ha de varial 
            x_kl = new Tensor( new Indice[]{ k_x ,
                                             l_x } ) ;
        }else{
            l_x = x_kl.darIndice("l") ;
            k_x = x_kl.darIndice("k") ;
        }
        
        
        Agrupador l_es = new Agrupador( new Indice[] {l_x,l_ent}) ;
        
        boolean desbor_l , 
                desbor_k ;
        
        //k_x.max-1 es el último valor del indice. La última entrada es el bias.
        //cómo mejorar esto todas son uno no hay información nueva en los l reales
        k_x.ponerA( k_x.max - 1); 
        for( desbor_l=false , l_es.reiniciar() ; !desbor_l ; desbor_l = l_es.inc())
            x_kl.fijarCompAct(1);
        
        
        //aquí se debería sincronizar las k-es, al ocurrir un desborde de uno se resetea el otro
        for( desbor_l=false , l_es.reiniciar() ; !desbor_l ; desbor_l = l_es.inc())
            for(desbor_k= false , k_ent.reiniciar() ; !desbor_k ; desbor_k = k_ent.inc() ){ 
                k_x.ponerA(k_ent.estado);
                x_kl.fijarCompAct(ent_kl.compAct());
            }
        
        
        //Vamos por el producto: calculamos la suma ponderada z_il
        Indice l_z , i_z;
        if(z_il == null){
            i_z = (Indice)i.clone() ;
            l_z = (Indice)l.clone() ;
            z_il= new Tensor(new Indice[]{   i_z , 
                                             l_z }) ;
        } else{
            l_z = z_il.darIndice("l") ;
            i_z = z_il.darIndice("i") ;
        }
        
        Indice k_w = w_ki.darIndice("k") ,
               i_w = w_ki.darIndice("i") ;
        
        
        //z_il = x_kl * w_ki , se contraen las k y varian juntas las i-es y las l-s
        z_il = x_kl.por( new Tensor[]{w_ki}, z_il  , 
                         new Indice[][]{{k_x,k_w}} ,  //los indices que se contraen
                         new Indice[][]{{i_z,i_w},
                                        {l_z,l_x}} ) ;//los indices que varian juntos
        
        
        //encontramos la salida y_il = sigma( z_il)
        Indice i_y , l_y ;
        if(y_il == null){
            i_y = (Indice)i.clone() ;
            l_y = (Indice)l.clone() ;
            y_il = new Tensor(new Indice[]{i_y,l_y}) ;
        }else{
            i_y = y_il.darIndice("i") ;
            l_y = y_il.darIndice("l") ;
        }
        
        Indice k_y_sig , l_y_sig ; //creo que está mala la nomenclatura debiera ser k_x_sig etc
        if(x_sig_kl == null){
            k_y_sig = (Indice)i.clone() ;
            k_y_sig.nombre = k.nombre   ;   //sirve de interfaz para la próxima capa
            l_y_sig = (Indice)l.clone() ;
            x_sig_kl = new Tensor(new Indice[]{k_y_sig,l_y_sig}) ;
        }else{
            k_y_sig= x_sig_kl.darIndice("k") ;
            l_y_sig = x_sig_kl.darIndice("l") ;
        }
        
        Agrupador i_es = new Agrupador(new Indice[]{i_y,i_z,k_y_sig}) ;
        l_es = new Agrupador(new Indice[]{l_y,l_z,l_y_sig}) ;
        boolean desbor_i ;
        
        float compAct ;
        for(desbor_i=false,i_es.reiniciar(); !desbor_i ; desbor_i = i_es.inc())
            for(desbor_l=false,l_es.reiniciar(); !desbor_l ; desbor_l = l_es.inc()){
                compAct = 1/(1+(float)Math.exp(-z_il.compAct())) ;
                y_il.fijarCompAct(compAct);
                x_sig_kl.fijarCompAct(compAct);
            }
        
        //calculamos la derivada de la función de activación dy_il calculada en z_il
        // dy_il = y_il (1 - y_il)
        Indice i_dy , l_dy ;
        if(dy_il == null){
            i_dy = (Indice)i.clone() ;
            l_dy = (Indice)l.clone() ;
            dy_il = new Tensor(new Indice[]{i_dy,l_dy}) ;
        }else{
            i_dy = dy_il.darIndice("i") ;
            l_dy = dy_il.darIndice("l") ;
        }
        
        i_es = new Agrupador(new Indice[]{i_dy,i_y}) ;
        l_es = new Agrupador(new Indice[]{l_dy,l_y}) ;
        
        
        for(desbor_i=false,i_es.reiniciar(); !desbor_i ; desbor_i = i_es.inc())
            for(desbor_l=false,l_es.reiniciar(); !desbor_l ; desbor_l = l_es.inc())
                dy_il.fijarCompAct(y_il.compAct()*(1-y_il.compAct())) ;
        
    }
    
    /*retroPaso -- llamada en la pasada hacia atras
    *   con ayuda del gradiente de la función de coste respecto de la variación
    *   de las salidas de esta capa: err_il, se halla
    *   el gradiente de la fun Cost respecto la variación de los parámetros: g_ik
    *   el gradiente de la fun cost respecto la variación de las entradas: erp_kl
    *   del último gradiente eliminamos la componente del gradiente para 
    *   la variación de la entrada agregada para el bias (un 1 constante): erp_ant_il
    */
    public void procesarError ( Tensor err_il ){
        Indice  i_err = err_il.darIndice("i") , 
                l_err = err_il.darIndice("l") ,
                i_g , k_g ;
        
        
        if(g_ki == null){
            i_g = (Indice)i.clone();
            k_g = (Indice)x_kl.darIndice("k").clone();
            g_ki = new Tensor( new Indice[] {i_g,k_g} ) ;
        } else {
            i_g = g_ki.darIndice("i") ;
            k_g = g_ki.darIndice("k") ;
        }
        
        Indice  k_x = x_kl.darIndice("k") ,
                l_x = x_kl.darIndice("l") ,
                i_dy = dy_il.darIndice("i"),
                l_dy = dy_il.darIndice("l") ;
        
        //Gradiente vs Parametros: g_ki = e_il * x_kl * dy_il
        Indice[]    k_es = {k_g,k_x},
                    i_es = {i_g,i_err,i_dy} ,
                    l_es = {l_err,l_x,l_dy} ; //solo esta se contrae
        
        Tensor b[] = {x_kl,dy_il} ;
        g_ki = err_il.por(b, g_ki, new Indice[][] {l_es}, new Indice[][]{k_es,i_es}) ;
        
        Indice k_erp , l_erp , k_w , i_w;
        if (erp_kl == null){
            k_erp = (Indice)k.clone() ; 
            l_erp = (Indice)l.clone() ;
            erp_kl = new Tensor(new Indice[]{k_erp,l_erp}) ;
        } else {
            k_erp = erp_kl.darIndice("k") ;
            l_erp = erp_kl.darIndice("l") ;
        }
        
        k_w = w_ki.darIndice("k") ;
        i_w = w_ki.darIndice("i") ;
        
        // Gradiente vs Entradas: erp_kl = e_il * dy_il * w_ki
        
        k_es = new Indice[] {k_erp,k_w} ;
        l_es = new Indice[] {l_erp,l_err,l_dy};
        i_es = new Indice[] {i_err,i_dy,i_w} ;
        
        b = new Tensor[]{dy_il,w_ki};
        erp_kl = err_il.por(b, erp_kl, new Indice[][]{i_es}, new Indice[][]{k_es,l_es}) ;
        
        Indice i_erp_ant , l_erp_ant ;
        if (erp_ant_il == null){
            i_erp_ant = (Indice)k.clone() ; 
            i_erp_ant.nombre = i.nombre ;
            l_erp_ant = (Indice)l.clone() ;
            erp_ant_il = new Tensor(new Indice[]{i_erp_ant,l_erp_ant}) ;
        } else {
            i_erp_ant = erp_ant_il.darIndice("i") ;
            l_erp_ant = erp_ant_il.darIndice("l") ;
        }
        
        Agrupador  iCam = new Agrupador( new Indice[]{k_erp,i_erp_ant}) ,
                    oL_es = new Agrupador( new Indice[]{l_erp,l_erp_ant});
        
        boolean desborI, desborL ;
        for( desborI = false , iCam.reiniciar(); !desborI ; desborI = iCam.inc() )
            for( desborL = false , oL_es.reiniciar(); !desborL ; desborL = oL_es.inc() )
                erp_ant_il.fijarCompAct(erp_kl.compAct());
        
        
    }
    
    //ajustar pesos
    public void variarPesos ( float paso ){
        // w_ki -= paso * g_ki ;
        
        Indice k_w, i_w, k_g , i_g ;
        k_w = w_ki.darIndice("k") ;
        i_w = w_ki.darIndice("i") ;
        k_g = g_ki.darIndice("k") ;
        i_g = g_ki.darIndice("i") ;
        
        Agrupador  k_es = new Agrupador(new Indice[] {k_w,k_g}) ,
                    i_es = new Agrupador(new Indice[] {i_w,i_g}) ;
        
        boolean desbor_k , desbor_i ;
        for(k_es.reiniciar(),desbor_k=false ; !desbor_k ; desbor_k = k_es.inc())
            for(i_es.reiniciar(),desbor_i=false ; !desbor_i ; desbor_i = i_es.inc())
                w_ki.fijarCompAct(w_ki.compAct()*(1-1e-3f)-paso*g_ki.compAct());
        
    }
    
    //da la magnitud de Grad de Param al cuadrado
    public float darMGP_2(){
        return g_ki.darMagCua() ;
    }
    
    
    public Tensor darSalida(){
        return y_il ;
    }
    
    
    public Tensor darEntradaSig(){
        return x_sig_kl ;
    }
    
    public Tensor darRetroError(){
        return erp_ant_il ;
    }
    
    public JPanel darVisor(){
        return null ;
    }
}
