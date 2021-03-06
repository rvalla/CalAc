/*/////////////////////////////////////////////////////////////////////////////////////
CalAc permite evaluar la factibilidad de posiciones fijas en instrumentos de cuerda
(violín, viola y violoncello). Muestra para cada cuerda la distancia entre la cejilla
y la ubicación de la altura buscada y la distancia que deberá abarcar la mano del
instrumentista. Los indicadores sobre la tastiera muestran la evaluación del bloque de
sonidos con un código de colores. La evaluación de los bloques de sonidos se basa en
un artículo disponible en: "http://www.academia.edu/1897713/Acerca_de_los_acordes_en
_los_instrumentos_de_cuerda_desde_su_visualizacion_en_la_tastiera_a_la_idea_de_
Ertugrul_Sevsay_".
/////////////////////////////////////////////////////////////////////////////////////*/
import javax.swing.JComponent;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLayeredPane;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JOptionPane;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.Soundbank;
import javax.sound.midi.MidiUnavailableException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.Arrays;
import java.net.URL;

/*/////////////////////////////////////////////////////////////////////////////////////
La clase interfaz construye la ventana del programa y contiene todos los métodos que
hacen posible su funcionamiento.
/////////////////////////////////////////////////////////////////////////////////////*/
class Interfaz implements ActionListener {

	//Variables globales
	Color negro = new Color(45, 45, 45);
	Color blanco = new Color(255, 255, 255);
	Color rojo = new Color(190, 40, 40);
	Color azul = new Color(90, 140, 190);
	JButton bNotas[][] = new JButton[5][12];
	JLabel octava[] = new JLabel[5];
	JLabel cuerdas[] = new JLabel[4];
	JLabel indicadores[][] = new JLabel[4][4];
	JRadioButton instrumento[] = new JRadioButton[3];
	JTextField distancias[] = new JTextField[4];
	JSlider slider = new JSlider();
	JButton borrar = new JButton();
	JButton verMemorias = new JButton();
	int instrumentoElegido = 0;
	int cuerda = 0;
	int acorde[] = new int[4];
	int factibilidad[] = new int[]{1, 1, 1, 1};
	boolean existeCejilla = false;
	int cejilla = 0;
	int posicionMemoria = 0;
	int posicionMemoriaVer = 0;
	int contadorEjecutar = 0;
	int datosMemoria[][] = new int[12][10];
	boolean hayInstrumentos = true;
	boolean mostrandoMemoria = false;
	String idioma[] = new String[10];
	Font classInfoFont;
	Font dataObtenidaFont;
	Synthesizer sint;
	MidiChannel canal;
	
	/*//////////
	Constructor
	/////////*/
	Interfaz(){
		
		//Construcción del sintetizador
		try {
			sint = MidiSystem.getSynthesizer();
			sint.open();
         	//Confirmación de la existencia de un banco de sonidos.
         	if (sint.getDefaultSoundbank().getInstruments() == null){
         		hayInstrumentos = false;
         	} else {         		
				canal = sint.getChannels()[0];
         	}
      	} catch (MidiUnavailableException e) {}
	
		getIdioma(System.getProperty("user.language"));
		
		construirVentana(System.getProperty("os.name"));
      	
	}
	
	
	/*///////////////////////////////////////////////////////
	Construcción y métodos de gestión de la interfaz gráfica.
	///////////////////////////////////////////////////////*/
	
	//Construcción de la ventana, agregado del panel principal.
	void construirVentana(String os) {
		
		JFrame v = new JFrame("CalAc");
		v.setDefaultCloseOperation(3);
		v.setResizable(false);
		
		//Definiendo formatos según sistema operativo
		if (os.startsWith("Windows")){
			classInfoFont = new Font("sansserif", Font.PLAIN, 12);
			dataObtenidaFont = new Font("monospace", Font.BOLD, 14);
		} else {
			classInfoFont = new Font("sansserif", Font.PLAIN, 12);
			dataObtenidaFont = new Font("monospace", Font.PLAIN, 14);
		}
        
        //Icono para windows y linux
        if (os.startsWith("Windows") || os.startsWith("Linux")){
	        v.setSize(770, 580);
	        URL iconoUrl = getClass().getResource("Icono.png");
			if (iconoUrl != null){
				ImageIcon icono = new ImageIcon(iconoUrl);
				v.setIconImage(icono.getImage());
			}
		} else {
			v.setSize(770, 570);
		}
		
		/*Inicialización de botones y etiquetas del teclado para poder ejecutar
		los métodos que los configuran.*/	
		for (int o = 0; o < bNotas.length; o++){
			octava[o] = new JLabel();
			for (int i = 0; i < bNotas[o].length; i++){
				bNotas[o][i] = new JButton();
			}
		}
		
		//Inicialización de las etiquetas para las cuerdas en la tastiera.
		for (int i = 0; i < cuerdas.length; i++){
			cuerdas[i] = new JLabel();
		}
		
		v.setLocationRelativeTo(null);
		v.add(construirPanel());
		v.setVisible(true);

	}
	
	//Construcción del panel principal.
	JPanel construirPanel(){
	
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));		
		p.add(Box.createRigidArea(new Dimension (10, 0)));
		p.add(construirPanelx());
		p.add(Box.createRigidArea(new Dimension (10, 0)));
		
		return p;		
	
	}
	
	JPanel construirPanelx(){
	
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		
		Dimension d = new Dimension(10, 0);		

		p.add(Box.createRigidArea(d));
		p.add(construirPanelA());
		p.add(Box.createRigidArea(d));
		p.add(construirPanelB());
		p.add(Box.createRigidArea(d));
	
		return p;
	
	}
	
	//Construcción del panel de la izquierda.
	JPanel construirPanelA(){
	
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
			JLayeredPane lp = new JLayeredPane();
			lp.setOpaque(true);
			lp.setBorder(BorderFactory.createTitledBorder(idioma[0]));
			lp.setPreferredSize(new Dimension (200, 476));
			
			etiquetarTastiera();
			for (int i = 0; i < cuerdas.length; i++){
				lp.add(cuerdas[i], new Integer(2));
			}	
			
			URL tastieraUrl = getClass().getResource("Imagenes/Tastiera.png");
			if (tastieraUrl != null){
				ImageIcon fondoTastiera = new ImageIcon(tastieraUrl);
				JLabel tastiera = new JLabel(fondoTastiera);
				tastiera.setSize(180, 456);
				tastiera.setLocation(20, 58);
				tastiera.setVisible(true);
				lp.add(tastiera, new Integer(0));
			} else {
				JOptionPane.showMessageDialog(new JFrame(),
   				"<html>Imágenes necesarias no disponibles.<br>Necessary images not available.</html>",
  				"Error",
			    JOptionPane.ERROR_MESSAGE);
			    System.exit(0);
			}
			
			construirIndicadores();
			for (int o = 0; o < indicadores.length; o++){
				for (int i = 0; i < indicadores[o].length; i++){
					lp.add(indicadores[o][i], new Integer(1));	
				}
			}
			
		
		p.add(Box.createRigidArea(new Dimension (0, 10)));
		p.add(lp);
		p.add(Box.createRigidArea(new Dimension (0, 10)));
		
		return p;

	}
	
	//Construcción del panel de la derecha.
	JPanel construirPanelB(){
	
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(540, 500));
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));	
		p.add(Box.createRigidArea(new Dimension (0, 10)));
		p.add(construirpTeclado());
		p.add(Box.createRigidArea(new Dimension (0, 10)));
		p.add(construirpResultados());
		p.add(Box.createRigidArea(new Dimension (0, 10)));
		
		return p;

	}
	
	//Construcción del panel que aloja el teclado.
	JPanel construirpTeclado(){
	
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.setMaximumSize(new Dimension(540, 150));
		p.add(Box.createRigidArea(new Dimension(10, 0)));
		
		JLayeredPane lp = new JLayeredPane();
		lp.setOpaque(true);
        lp.setBorder(BorderFactory.createTitledBorder(idioma[1]));
        
		Font numeroOctava = new Font("sansserif", Font.PLAIN, 11);
		for (int o = 0; o < octava.length; o++){
			octava[o].setText(String.valueOf(o + 2));
			octava[o].setLocation((98 * o) + 18, 25);
			octava[o].setOpaque(true);
			octava[o].setSize(20, 20);
			octava[o].setVisible(true);
			octava[o].setFont(numeroOctava);
			lp.add(octava[o], new Integer(1));
		}
		
		construirTeclado();
		 		
   		for (int o = 0; o < bNotas.length; o++){
        	for (int i = 0; i < bNotas[o].length; i++){
        
        		if (esTeclaNegra(i) == true){
        			lp.add(bNotas[o][i], new Integer(2));
        		} else {
        			lp.add(bNotas[o][i], new Integer(1));
       		 	}
		
			}        
        }
        
		p.add(lp);
		p.add(Box.createRigidArea(new Dimension(10, 0)));
	
		return p;
		
	}
	
	//Construcción del panel que mostrará los resultados.
	JPanel construirpResultados(){
	
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(Box.createRigidArea(new Dimension(10, 0)));	
		p.add(panelR());
		p.add(Box.createRigidArea(new Dimension(10, 0)));
	
		return p;
		
	}
	
	JPanel panelR(){
	
		JPanel p = new JPanel();
		Dimension d = new Dimension(0, 10);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setMaximumSize(new Dimension(540, 400));
		p.setBorder(BorderFactory.createTitledBorder(idioma[2]));
		
			JPanel pp = new JPanel();
			pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
			
			for (int i = 0; i < instrumento.length; i++){
				instrumento[i] = new JRadioButton();
				instrumento[i].setFont(dataObtenidaFont);
				instrumento[i].setVisible(true);
				instrumento[i].addActionListener(this);
				if (i == 0){
					instrumento[i].setSelected(true);
				}
			}
			instrumento[0].setText(idioma[3]);
			instrumento[1].setText("Viola");
			instrumento[2].setText("Violoncello");
			
			Dimension dd = new Dimension(3, 0);
			pp.add(instrumento[0]);
			pp.add(Box.createRigidArea(dd));
			pp.add(instrumento[1]);
			pp.add(Box.createRigidArea(dd));
			pp.add(instrumento[2]);
		
		p.add(Box.createRigidArea(new Dimension(0, 35)));
		p.add(pp);
		p.add(Box.createRigidArea(new Dimension(0, 20)));
		p.add(panelDatos());
		p.add(Box.createRigidArea(d));
		p.add(panelS());
		p.add(Box.createRigidArea(d));
		p.add(panelBotones());
		p.add(Box.createRigidArea(new Dimension(0, 30)));
		p.add(panelCI());
		p.add(Box.createRigidArea(d));
			
		return p;
	
	}
	
	//Método para desactivar los botones de selección de instrumentos.
	void desactivarInstrumentos(){
	
		for (int i = 0; i < instrumento.length; i++){
			instrumento[i].setSelected(false);
		}
	
	}
	
	JPanel panelDatos(){
	
		JPanel p = new JPanel();
		p.setOpaque(true);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.setBorder(BorderFactory.createTitledBorder(idioma[4]));
		p.setMaximumSize(new Dimension(420, 80));
		p.add(Box.createRigidArea(new Dimension(44, 0)));
		
			JLabel c[] = new JLabel[4];
			JPanel pp[] = new JPanel[4];			
			
			for ( int i = 0; i < c.length; i++){
				c[i] = new JLabel();
				c[i].setFont(classInfoFont);
				distancias[i] = new JTextField();
				distancias[i].setHorizontalAlignment(JTextField.CENTER);
				distancias[i].setEditable(false);
				distancias[i].setBackground(blanco);
				distancias[i].setSize(60, 30);
				distancias[i].setVisible(true);
				
				pp[i] = new JPanel();
				pp[i].setOpaque(true);
				pp[i].setLayout(new BoxLayout(pp[i], BoxLayout.X_AXIS));
				pp[i].setMaximumSize(new Dimension(70, 23));
				pp[i].add(c[i]);
				pp[i].add(Box.createRigidArea(new Dimension(2, 0)));
				pp[i].add(distancias[i]);
				
				p.add(pp[i]);
				if (i < c.length - 1){
					p.add(Box.createRigidArea(new Dimension(15, 0)));
				}
				
			}
			
			c[0].setText("IV");
			c[1].setText("III");
			c[2].setText("II");
			c[3].setText("I");
		
		return p;	
	
	}
	
	//Construcción del panel que aloja el slider que muestra la distancia a cubrir.
	JPanel panelS(){
	
		JPanel p = new JPanel();
		p.setOpaque(true);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createTitledBorder(idioma[5]));
		p.setMaximumSize(new Dimension(420, 80));
		
			slider = new JSlider(JSlider.HORIZONTAL, 0, 18, 0);
			slider.setPaintLabels(true);
				
				Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
		    	table.put (5, new JLabel(idioma[6]));
    			table.put (12, new JLabel(idioma[7]));
			
    		slider.setLabelTable(table);
			slider.setVisible(true);
			slider.setMajorTickSpacing(4);
			slider.setMinorTickSpacing(1);
			slider.setPaintTicks(true);
	
		p.add(slider);
	
		return p;
	
	}
	
	JPanel panelBotones(){
	
		JPanel p = new JPanel();
		p.setOpaque(true);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.setMaximumSize(new Dimension(200, 30));
		
		verMemorias.setText(idioma[8]);
		verMemorias.setVisible(true);
		verMemorias.addActionListener(this);

		borrar.setText(idioma[9]);
		borrar.setVisible(true);
		borrar.addActionListener(this);
		
		Dimension d = new Dimension(15, 0);
		
		p.add(verMemorias);
		p.add(Box.createRigidArea(d));
		p.add(borrar);
	
		return p;
	
	}
	
	JPanel panelCI(){
	
		JPanel p = new JPanel();
		p.setOpaque(true);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		
		JLabel classInfo = new JLabel("<html><div align='center'>http://sourceforge.net/projects/calac<br>Rodrigo Valla</div><html>", JLabel.CENTER);
	
		p.add(classInfo);
	
		return p;
	
	}
	
	void etiquetarTastiera(){
		
		Font tastiera = new Font("sansserif", Font.BOLD, 14);
		int pinicial = 60;
		for (int i = 0; i < cuerdas.length; i++){
			cuerdas[i].setSize(24, 24);
			if (i == 2){
				pinicial = 62;
			}
			cuerdas[i].setLocation(pinicial + (36 * i), 34);
			cuerdas[i].setFont(tastiera);
			cambiarCuerdas(0);
			cuerdas[i].setVisible(true);
		}
		
	}
				
	//Método para etiquetar las cuerdas de la tastiera.
	void cambiarCuerdas(int x){
		
		if (x == 0){
			cuerdas[0].setText("G");
			cuerdas[1].setText("D");
			cuerdas[2].setText("A");
			cuerdas[3].setText("E");
		} else if (x == 1){
			cuerdas[0].setText("C");
			cuerdas[1].setText("G");
			cuerdas[2].setText("D");
			cuerdas[3].setText("A");
		}
	
	}
	
	void getIdioma(String s){
	
		if (s.equals("es")){
			idioma[0] = "Disposición del acorde";
			idioma[1] = "Teclado";
			idioma[2] = "Análisis";
			idioma[3] = "Violín";
			idioma[4] = "Distancia desde la cejilla";
			idioma[5] = "Distancia a cubrir";
			idioma[6] = "4ta.J";
			idioma[7] = "8va.";
			idioma[8] = "Historial";
			idioma[9] = "Borrar";
		} else {
			idioma[0] = "Chord arrangement";
			idioma[1] = "Keyboard";
			idioma[2] = "Analysis";
			idioma[3] = "Violin";
			idioma[4] = "Distance from nut";
			idioma[5] = "Distance to cover";
			idioma[6] = "p4th.";
			idioma[7] = "8ve.";
			idioma[8] = "History";
			idioma[9] = "Erase";
		}	
		
	}
	
	/*////////////////////////////////////////////////////////
	Métodos para la construcción y gestión de los indicadores.
	////////////////////////////////////////////////////////*/
	void construirIndicadores(){
	
		URL indicadorVerdeUrl = getClass().getResource("Imagenes/IndicadorVerde.png");
		URL indicadorNegroUrl = getClass().getResource("Imagenes/IndicadorNegro.png");
		URL indicadorAmarilloUrl = getClass().getResource("Imagenes/IndicadorAmarillo.png");
		URL indicadorRojoUrl = getClass().getResource("Imagenes/IndicadorRojo.png");
		
		ImageIcon indicadorVerde;
		ImageIcon indicadorNegro;
		ImageIcon indicadorAmarillo;
		ImageIcon indicadorRojo;

		if (indicadorVerdeUrl != null && indicadorNegroUrl != null 
				&& indicadorAmarilloUrl != null && indicadorRojoUrl != null){
			
			indicadorVerde = new ImageIcon(indicadorVerdeUrl);
			indicadorNegro = new ImageIcon(indicadorNegroUrl);
			indicadorAmarillo = new ImageIcon(indicadorAmarilloUrl);
			indicadorRojo = new ImageIcon(indicadorRojoUrl);
			
			for (int o = 0; o < indicadores.length; o++){
				indicadores[o][0] = new JLabel(indicadorVerde);
				indicadores[o][1] = new JLabel(indicadorNegro);
				indicadores[o][2] = new JLabel(indicadorAmarillo);
				indicadores[o][3] = new JLabel(indicadorRojo);
				for (int i = 0; i < indicadores[o].length; i++){
					indicadores[o][i].setSize(24, 24);
					indicadores[o][i].setLocation(54 + (36 * o), 10);
					indicadores[o][i].setVisible(false);
				}
			}
			
		} else {
			JOptionPane.showMessageDialog(new JFrame(),
   				"<html>Imágenes necesarias no disponibles.<br>Necessary images not available.</html>",
  				"Error",
			    JOptionPane.ERROR_MESSAGE);
			    System.exit(0);
		}
		
	}
	
	//Método para desactivar los indicadores.
	void desactivarIndicadores(){
	
		for (int o = 0; o < indicadores.length; o++){
			for (int i = 0; i < indicadores[o].length; i++){
			
				if (indicadores[o][i].isVisible() == true){
					indicadores[o][i].setVisible(false); 
				}
			
			}
		}
	
	}
	
	/*/////////////////////////////////////////////////
	Métodos para la construcción y gestión del teclado.
	/////////////////////////////////////////////////*/
	void construirTeclado(){
	
		for (int o = 0; o < bNotas.length; o ++){
			for (int i = 0; i < bNotas[o].length; i++){
			
       			bNotas[o][i].setOpaque(true);
   				bNotas[o][i].setBorder(null);
				bNotas[o][i].setBorderPainted(false);
				bNotas[o][i].addActionListener(this);
			
				//Diferenciación del tamaño de las teclas.
				if (esTeclaNegra(i) == true){			
					bNotas[o][i].setSize(6, 35);
				} else {
					bNotas[o][i].setSize(12, 60);
				}
			
	        	pintarTeclado();
				posicionarTeclado();
	        	bNotas[o][i].setVisible(true);
	        	
        	}	
		} 
		
	}
	
	//Método para pintar el teclado a blanco y negro.
	void pintarTeclado(){
	
		for (int o = 0; o < bNotas.length; o ++){
			for (int i = 0; i < bNotas[o].length; i++){
			
				if (esTeclaNegra(i) == true){			
					bNotas[o][i].setBackground(negro);
				} else {
					bNotas[o][i].setBackground(blanco);
				}
			
			}
		}
	
	}
	
	//Método para pintar una determinada tecla del teclado.
	void pintarTecla(int o, int i){
	
		if (esTeclaNegra(i) == true){			
			bNotas[o][i].setBackground(azul);
		} else {
			bNotas[o][i].setBackground(rojo);
		}
	
	}
	
	/* Método que posiciona las teclas (pnicial corresponde al margen con respecto al
	borde del panel, ajuste permite corregir el error que se produce cuando aparecen
	dos teclas blancas consecutivas, ajusteNegras permite centrar las teclas negras con
	respecto a las blancas y ajusteOctava ubica las octavas en forma sucesiva.*/
	void posicionarTeclado(){
	
		int pinicial = 14;
		int ajuste = 0;
		int ajusteNegras = 3;
		int ajusteOctava = 98;
		
		for (int o = 0; o < bNotas.length; o++){
			
			ajuste = 0;
			
			for (int i = 0; i < bNotas[o].length; i++){
			
				if (i >= 5){
					ajuste = 7;
				}
			
				if (esTeclaNegra(i) == true){			
					bNotas[o][i].setLocation(pinicial + (7 * i) + ajuste + (ajusteOctava * o) + ajusteNegras, 45);
				} else {
					bNotas[o][i].setLocation(pinicial + (7 * i) + ajuste + (ajusteOctava * o), 45);
				}
			
			}
		}
	
	}
	
	//Métodos para activar y desactivar el teclado.
	void activarTeclado(){
		for (int o = 0; o < bNotas.length; o++){
			for (int i=0; i < bNotas[o].length; i++){
				bNotas[o][i].setEnabled(true);
			}
		}
	}

	void desactivarTeclado(){
		for (int o = 0; o < bNotas.length; o++){
			for (int i=0; i < bNotas[o].length; i++){
				bNotas[o][i].setEnabled(false);
			}
		}
	}

	//Método para decidir si el botón corresponde a una tecla blanca o negra.
	boolean esTeclaNegra(int i){
	
		boolean esTN = false;
		if (i == 1 || i == 3 || i == 6 || i == 8 || i == 10){
			esTN = true;
		}
		return esTN;
	
	}
	
	/*//////////////////////////////////////////
	Métodos para el funcionamiento del programa
	//////////////////////////////////////////*/
	public void actionPerformed (ActionEvent ae) {
	
		for (int o = 0; o < bNotas.length; o++){
			for (int i=0; i < bNotas[o].length; i++ ){
			
				if(ae.getSource() == bNotas[o][i]){
			
					if (cuerda == 0){
						borrar();
					}
					
					acorde[cuerda] = (12 * o) + i - getAjuste() - (cuerda * 7);
					
					if (acorde[cuerda] < 20){
						pintarTecla(o, i);
						procesarAcorde(acorde[cuerda]);
					}
					
					if (hayInstrumentos == true){
						playN((12 * o) + 36 + i);
					}
				
				}
						
			}
		}
		
		if (ae.getSource() == instrumento[0]) {
			instrumentoElegido = 0;
			if (cuerda != 0){
				guardarMemoria();
				datosMemoria[posicionMemoria - 1][9] = datosMemoria[posicionMemoria -1][9] - 1;
			}
			borrar();
			instrumento[1].setSelected(false);
			instrumento[2].setSelected(false);
			cambiarCuerdas(0);
		}
		
		if (ae.getSource() == instrumento[1]) {
			instrumentoElegido = 1;
			if (cuerda != 0){
				guardarMemoria();
				datosMemoria[posicionMemoria - 1][9] = datosMemoria[posicionMemoria -1][9] - 1;
			}
			borrar();
			instrumento[0].setSelected(false);
			instrumento[2].setSelected(false);
			cambiarCuerdas(1);
		}
		
		if (ae.getSource() == instrumento[2]) {
			instrumentoElegido = 2;
			if (cuerda != 0){
				guardarMemoria();
				datosMemoria[posicionMemoria - 1][9] = datosMemoria[posicionMemoria -1][9] - 1;
			}
			borrar();
			instrumento[1].setSelected(false);
			instrumento[0].setSelected(false);
			cambiarCuerdas(1);
		}
		
		if (ae.getSource() == borrar) {
			if (cuerda != 0){
				guardarMemoria();
				datosMemoria[posicionMemoria - 1][9] = datosMemoria[posicionMemoria -1][9] - 1;
			}
			borrar();
		}
		
		if(ae.getSource() == verMemorias){
			
			pintarTeclado();
			desactivarIndicadores();
			desactivarInstrumentos();
			borrarDistancias();

			if (0 < contadorEjecutar && contadorEjecutar < 12) {
				
				if(0 < posicionMemoriaVer){
					mostrarMemoria(posicionMemoriaVer);
					posicionMemoriaVer = posicionMemoriaVer - 1;
				} else {
					mostrarMemoria(posicionMemoriaVer);
					posicionMemoriaVer = contadorEjecutar - 1;
				}

			} else if (11 < contadorEjecutar){

				if (posicionMemoriaVer < 12){
					posicionMemoriaVer = posicionMemoriaVer + 12;
				}
			
				mostrarMemoria(posicionMemoriaVer - 12);
				posicionMemoriaVer = posicionMemoriaVer - 1;

			} else {
			
			}

		}
	
	}
	
	//Método para borrar todos los datos.
	void borrar(){
		
		mostrandoMemoria = false;
		borrarAcorde();
		pintarTeclado();
		activarTeclado();
		desactivarIndicadores();
		borrarDistancias();
		slider.setValue(0);
	
	}
	
	//Método para borrar los cuadro de texto que muestran las distancias desde la cejilla.
	void borrarDistancias(){
		for (int i = 0; i < distancias.length; i++){
			distancias[i].setText("");
		}
	}
	
	/*///////////////////////////////////
	Métodos para al producción de sonido.
	///////////////////////////////////*/
	
	//Método para hacer sonar las teclas.
	void playN(int n){
	
		canal.noteOn(n, 100);
       	try {
       		Thread.sleep(10);
           	} catch (InterruptedException e) {
          
           	} finally {
           	canal.noteOff(n);
           	}
        
    }
    
    /*////////////////////////////////
	Gestión del historial de análisis
	////////////////////////////////*/
	void mostrarMemoria(int x){
			
		if (mostrandoMemoria == false){
			mostrandoMemoria = true;
			desactivarTeclado();
		}
		
		instrumento[datosMemoria[x][8]].setSelected(true);
		slider.setValue(calcularDistanciaMemoria(datosMemoria[x]));
		pintarTeclas(getTeclas(datosMemoria[x]));
		
		for (int i = 0; i < datosMemoria[x][9] + 1; i++){
			getDedo(datosMemoria[x][i], i, datosMemoria[x][i + 4]);
			distancias[i].setText(String.valueOf(datosMemoria[x][i]));
		}
		
	}

	void gestionarPosicionMemoria(){
		
		if (contadorEjecutar == 24){
			contadorEjecutar = contadorEjecutar - 11;
		} else {
			contadorEjecutar = contadorEjecutar + 1;
		}
		
		if (posicionMemoria == 11){
			posicionMemoria = 0;
		} else {
			posicionMemoria = posicionMemoria + 1;
		}
			
	}
	
	void guardarMemoria(){
	
		for (int i = 0; i < 4; i++){
			datosMemoria[posicionMemoria][i] = acorde[i];
			datosMemoria[posicionMemoria][i + 4] = factibilidad[i];			
		}
		
		datosMemoria[posicionMemoria][8] = instrumentoElegido;
		datosMemoria[posicionMemoria][9] = cuerda;
		posicionMemoriaVer = contadorEjecutar;
		gestionarPosicionMemoria();
		
	}
	
	void pintarTeclas(int[] x){
		
		int octava;
		int nota;
		for (int i = 0; i < x.length; i++){
			nota = x[i]%12;
			octava = (x[i] - nota) / 12;
			pintarTecla(octava, nota);
		}
	
	}
	
	int[] getTeclas(int[] x){
	
		int t[] = new int[x[9] + 1];
		int ajuste = 0;
		if (x[8] == 0){
			ajuste = 19;
		} else if (x[8] == 1) {
			ajuste = 12;
		}

		for (int i = 0; i < t.length; i++){
			t[i] = x[i] + ajuste + (i * 7);
		}
		
		return t;
	
	}
	
	int calcularDistanciaMemoria(int[] x){
	
		int a[] = new int[4];
		for (int i = 0; i < a.length; i++){
			a[i] = x[i];
		}
		int d = calcularDistancia(a);
		
		return d;
	
	}
	
	/*///////////////////
	Gestión del análisis
	///////////////////*/
	void procesarAcorde(int x){
	
			distancias[cuerda].setText(String.valueOf(x));
			evaluarAcorde(acorde);
			getDedo(x, cuerda, factibilidad[cuerda]);
			slider.setValue(calcularDistancia(acorde));
			if (cuerda == 3){
				guardarMemoria();
			}
			gestionarCuerda();
	
	}
	
	/*Método que recibe el acorde completo y evalúa su factibilidad considerando las
	distintas situaciones límite*/
	void evaluarAcorde(int x[]){
		
		/*Condicional que distingue entre cuerdas al aire, alturas ubicadas en la
		cuerda y alturas que por ser más graves que la cuerda al aire no pueden
		ser realizadas.*/
		if (x[cuerda] < 0){
			indicadores[cuerda][factibilidad[cuerda]].setVisible(false);
			factibilidad[cuerda] = 3;
		} else if (x[cuerda] == 0){
			indicadores[cuerda][factibilidad[cuerda]].setVisible(false);
			factibilidad[cuerda] = 0;
		} else {
			indicadores[cuerda][factibilidad[cuerda]].setVisible(false);
			factibilidad[cuerda] = 1;
		}
		
		//Condicional que evalúa las situaciones difíciles entre las dos primeras notas.
		if (cuerda == 1){
			
			if (x[1] == x[0] && x[1] > 0){
				if (instrumentoElegido == 2){
					existeCejilla = true;
				}
				indicadores[0][factibilidad[0]].setVisible(false);
				factibilidad[0] = 2;
				getDedo(x[0], 0, factibilidad[0]);
				factibilidad[1] = 2;				
			}
				
		}
		
		//Condicional que evalúa las situaciones difíciles entre las tres primeras notas.
		if (cuerda == 2){
		
			if (x[2] == x[1] && x[1] == x[0] && x[2] > 0){
				if (instrumentoElegido == 2){
					factibilidad[2] = 2;
					if (existeCejilla == false){
						existeCejilla = true;
					}	
				} else {
					indicadores[0][factibilidad[0]].setVisible(false);
					indicadores[1][factibilidad[1]].setVisible(false);
					factibilidad[0] = 3;
					factibilidad[1] = 3;
					getDedo(x[0], 0, factibilidad[0]);
					getDedo(x[1], 1, factibilidad[1]);
					factibilidad[2] = 3;	
				}
			}
			
			if (x[2] == x[0] && x[2] != x[1] && x[2] > 0){
				if (instrumentoElegido == 2){
					indicadores[0][factibilidad[0]].setVisible(false);
					factibilidad[0] = 2;
					getDedo(x[0], 0, factibilidad[0]);
					if (x[1] < x[2]  && x[1] > 0){
						indicadores[1][factibilidad[1]].setVisible(false);
						factibilidad[1] = 3;
						getDedo(x[1], 1, factibilidad[1]);
					}
					factibilidad[2] = 2;
					if (existeCejilla == false){
						existeCejilla = true;
					}
				} else {
					indicadores[0][factibilidad[0]].setVisible(false);
					factibilidad[0] = 3;
					getDedo(x[0], 0, factibilidad[0]);
					factibilidad[2] = 3;	
				}
			}
			
			if (x[2] == x[1] && x[2] != x[0] && x[2] > 0){
				if (instrumentoElegido == 2){
					indicadores[1][factibilidad[1]].setVisible(false);
					factibilidad[1] = 2;
					getDedo(x[1], 1, factibilidad[1]);
					if (x[0] < x[2]  && x[0] > 0){
						indicadores[0][factibilidad[0]].setVisible(false);
						factibilidad[0] = 3;
						getDedo(x[0], 0, factibilidad[0]);
					}
					factibilidad[2] = 2;	
					if (existeCejilla == false){
						existeCejilla = true;
						cejilla = 1;
					}
				} else {
					indicadores[1][factibilidad[1]].setVisible(false);
					factibilidad[1] = 2;
					getDedo(x[1], 1, factibilidad[1]);
					factibilidad[2] = 2;	
				}
			}
			
			if (x[0] == x[1] && x[2] < x[0] && x[2] > 0){
				if (instrumentoElegido == 2){
					factibilidad[2] = 3;
				}
			}
		
		}
		
		//Condicional que evalúa las situaciones difíciles entre las cuatro notas.
		if (cuerda == 3){
			if (x[3] == x[2] && x[2] == x[1] && x[1] == x[0] && x[3] > 0){
				if (instrumentoElegido == 2){
					factibilidad[3] = 2;	
				} else {
					factibilidad[3] = 3;	
				}
			}
			
			if (x[3] == x[0] && x[3] > 0){
				if (instrumentoElegido == 2){
					if (existeCejilla == true){
						if (x[cejilla] == x[3]){
							factibilidad[3] = 2;
						} else if (x[cejilla] < x[3]){
							indicadores[0][factibilidad[0]].setVisible(false);
							factibilidad[0] = 3;
							getDedo(x[0], 0, factibilidad[0]);
							factibilidad[3] = 3;
						} else {
							indicadores[0][factibilidad[0]].setVisible(false);
							factibilidad[0] = 2;
							getDedo(x[0], 0, factibilidad[0]);
							indicadores[1][factibilidad[1]].setVisible(false);
							factibilidad[1] = 3;
							getDedo(x[1], 1, factibilidad[1]);
							indicadores[2][factibilidad[2]].setVisible(false);
							factibilidad[2] = 3;
							getDedo(x[2], 2, factibilidad[2]);
							factibilidad[3] = 2;
						}
						cejilla = 0;
					} else {
						indicadores[0][factibilidad[0]].setVisible(false);
						factibilidad[0] = 2;
						getDedo(x[0], 0, factibilidad[0]);
						if (x[1] < x[3]  && x[1] > 0){
							indicadores[1][factibilidad[1]].setVisible(false);
							factibilidad[1] = 3;
							getDedo(x[1], 1, factibilidad[1]);
						}
						if (x[2] < x[3]  && x[2] > 0){
							indicadores[2][factibilidad[2]].setVisible(false);
							factibilidad[2] = 3;
							getDedo(x[2], 2, factibilidad[2]);
						}
						factibilidad[3] = 2;	
						existeCejilla = true;
					}
				} else {
					if (x[3] != x[2]){
						indicadores[0][factibilidad[0]].setVisible(false);
						factibilidad[0] = 3;
						getDedo(x[0], 0, factibilidad[0]);
					}
					if (x[1] == x[3]){
						indicadores[1][factibilidad[1]].setVisible(false);
						factibilidad[1] = 3;
						getDedo(x[1], 1, factibilidad[1]);
					}
					factibilidad[3] = 3;	
				}
			}
			
			if (x[3] == x[1] && x[3] > 0){
				if (instrumentoElegido == 2){
					if (existeCejilla == true){
						if (x[cejilla] == x[3]){
							factibilidad[3] = 2;
						} else if (x[cejilla] < x[3] && x[0] > 0){
							indicadores[1][factibilidad[1]].setVisible(false);
							factibilidad[1] = 3;
							getDedo(x[1], 1, factibilidad[1]);
							factibilidad[3] = 3;
						} else {
							indicadores[1][factibilidad[1]].setVisible(false);
							factibilidad[1] = 2;
							getDedo(x[1], 1, factibilidad[1]);
							indicadores[0][factibilidad[0]].setVisible(false);
							factibilidad[0] = 3;
							getDedo(x[0], 0, factibilidad[0]);
							indicadores[2][factibilidad[2]].setVisible(false);
							factibilidad[2] = 3;
							getDedo(x[2], 2, factibilidad[2]);
							factibilidad[3] = 2;
						}
						cejilla = 1;
					} else {
						indicadores[1][factibilidad[1]].setVisible(false);
						factibilidad[1] = 2;
						getDedo(x[1], 1, factibilidad[1]);
						if (x[0] < x[3]  && x[0] > 0){
							indicadores[0][factibilidad[0]].setVisible(false);
							factibilidad[0] = 3;
							getDedo(x[0], 0, factibilidad[0]);
						}
						if (x[2] < x[3]  && x[2] > 0){
							indicadores[2][factibilidad[2]].setVisible(false);
							factibilidad[2] = 3;
							getDedo(x[2], 2, factibilidad[2]);
						}
						factibilidad[3] = 2;
						existeCejilla = true;
						cejilla = 1;
					}
				} else {
					indicadores[1][factibilidad[1]].setVisible(false);
					factibilidad[1] = 3;
					getDedo(x[1], 1, factibilidad[1]);
					if (x[3] == x[0] && factibilidad[0] != 3){
						indicadores[0][factibilidad[0]].setVisible(false);
						factibilidad[0] = 3;
						getDedo(x[0], 0, factibilidad[0]);
					}
					if (x[3] == x[2] && factibilidad[2] != 3){
						indicadores[2][factibilidad[2]].setVisible(false);
						factibilidad[2] = 3;
						getDedo(x[2], 2, factibilidad[2]);
					}
					factibilidad[3] = 3;	
				}
			}
			
			if (x[3] == x[2] && x[3] > 0){
				if (instrumentoElegido == 2){
					if (existeCejilla == true){
						if (x[cejilla] == x[3]){
							factibilidad[3] = 2;
						} else if (x[cejilla] < x[3] && x[0] > 0){
							indicadores[2][factibilidad[2]].setVisible(false);
							factibilidad[2] = 3;
							getDedo(x[2], 2, factibilidad[2]);
							factibilidad[3] = 3;
						} else {
							indicadores[0][factibilidad[0]].setVisible(false);
							factibilidad[0] = 3;
							getDedo(x[0], 0, factibilidad[0]);
							indicadores[1][factibilidad[1]].setVisible(false);
							factibilidad[1] = 3;
							getDedo(x[1], 1, factibilidad[1]);
							indicadores[2][factibilidad[2]].setVisible(false);
							factibilidad[2] = 2;
							getDedo(x[2], 2, factibilidad[2]);
							factibilidad[3] = 2;
						}
						cejilla = 2;
					} else {
						indicadores[2][factibilidad[2]].setVisible(false);
						factibilidad[2] = 2;
						getDedo(x[2], 2, factibilidad[2]);
						if (x[0] < x[3]  && x[0] > 0){
							indicadores[0][factibilidad[0]].setVisible(false);
							factibilidad[0] = 3;
							getDedo(x[0], 0, factibilidad[0]);
						}
						if (x[1] < x[3]  && x[1] > 0){
							indicadores[1][factibilidad[1]].setVisible(false);
							factibilidad[1] = 3;
							getDedo(x[1], 1, factibilidad[1]);
						}
						factibilidad[3] = 2;
						existeCejilla = true;
						cejilla = 2;
					}
				} else {
					if (x[0] != x[3] && x[1] != x[3]){
						indicadores[2][factibilidad[2]].setVisible(false);
						factibilidad[2] = 2;
						getDedo(x[2], 2, factibilidad[2]);
						factibilidad[3] = 2;
					}
					if (x[0] == x[3] && x[1] != x[3]){
						factibilidad[3] = 3;						
					}
					if (x[1] == x[3] && x[0] != x[3]){
						indicadores[1][factibilidad[1]].setVisible(false);
						indicadores[2][factibilidad[2]].setVisible(false);
						factibilidad[1] = 3;
						factibilidad[2] = 3;
						getDedo(x[1], 1, factibilidad[1]);
						getDedo(x[2], 2, factibilidad[2]);
						factibilidad[3] = 3;						
					}
				}	
						
			}
			
			if (existeCejilla == true && x[3] < x[cejilla] && x[3] > 0 && x[3]!= x[cejilla]){
				factibilidad[3] = 3;
			}
			
		}
		
	}
	
	//Método que recibe nota, cuerda y dificultad para mostrar la ubicación de la altura.
	void getDedo(int x, int y, int z){
	
		if (x >= 0){
			indicadores[y][z].setLocation(indicadores[y][0].getX(), 34 + (24 * x));
			indicadores[y][z].setVisible(true);
		} else {
			indicadores[y][z].setLocation(indicadores[y][0].getX(), 14);
			indicadores[y][z].setVisible(true);
		}
	
	}
	
	/*Método que recibe el acorde completo y calcula la distancia total a cubrir por
	la mano del instrumentista.*/
	int calcularDistancia (int x[]){
	
		int dis[] = new int[x.length];
		for (int i = 0; i < x.length; i++){
			dis[i] = x[i];
		}
		Arrays.sort(dis);
		
		int a = 0;
		while (dis[a] <= 0){
			if (a < 3){
				a = a + 1;
			} else {
				break;
			}
		}
		
		int d = 0;
		if (a < 3){
			d = dis[3] - dis[a];
		}
		
		return d;
			
	}
	
	void borrarAcorde(){
	
		for (int i  = 0; i < acorde.length; i++){
			acorde[i] = 0;
			factibilidad[i] = 0;
		}
		cuerda = 0;
		existeCejilla = false;
		cejilla = 0;
	
	}
	
	//Método para conocer a qué cuerda corresponderá la siguiente altura.
	void gestionarCuerda(){
	
		if (cuerda == 3){
			cuerda = 0;
		} else {
			cuerda = cuerda + 1;
		}
	
	}
	
	//Método que devuelve una constante de ajuste para el instrumento elegido.
	int getAjuste(){
		
		int ajuste = 0;
		if (instrumentoElegido == 0){
			ajuste = 19;
		} else if (instrumentoElegido == 1) {
			ajuste = 12;		
		} 
		
		return ajuste;
	
	}

}