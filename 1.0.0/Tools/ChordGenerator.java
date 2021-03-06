import java.io.*;
import java.util.Random;
import java.util.Arrays;

//Program for Aleatory Orchestral String Chord Generation
class ChordGenerator {

	static Random r;	
	
	public static void main(String[] args) throws Exception{
		
		r = new Random();
		
		System.out.println();
		System.out.println("////////////////////////////////////////////////////");
		System.out.println("	ORCHESTRAL STRING'S CHORD GENERATOR");
		System.out.println("////////////////////////////////////////////////////");		
		System.out.println();
		System.out.println("Type 'number of chords' space 'instrument to consider'");
		System.out.println("Instrument code: 1. Violin, 2. Viola, 3. Cello");
		System.out.println();
		
		ChordGenerator();
		
	}
	
	public static void ChordGenerator() throws Exception{
	
		
		
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);

		String cantidad;
		cantidad = br.readLine();
		String valores [] = cantidad.split(" ");
		System.out.println();
		
		int c;
		int d;
		if (valores.length == 2){
			try	{
				c = (Integer.parseInt(valores[0]));
				d = (Integer.parseInt(valores[1]));
				if (d < 1 || d > 3){
					System.out.println("Instrument code not valid. Retry:");
					System.out.println();
					ChordGenerator();
				} else { 
					getChords(c, d);
				}
			} catch (NumberFormatException e){
				System.out.println();
				System.out.println("'" + cantidad + "' IS NOT VALID");
				System.out.println("Retry:");
				System.out.println();
				ChordGenerator();
			}
		} else {
			System.out.println();
			System.out.println("'" + cantidad + "' IS NOT VALID");
			System.out.println("Retry:");
			System.out.println();
			ChordGenerator();
		}	
		
		
		exit();
	
	}
	
	/*Method to produce chords that recieves chord quantity and instrument to
	consider (1. Violin, 2. Viola, 3. Cello). Returns a list of chords indicating:
	Midi notes, Notes (0-11), Distance from nut, Note evaluation, Distance to be
	covered and a feasibility estimation.*/
	public static void getChords(int x, int y){
	
		int s = 0;
		for (int i = 0; i < x; i++){
		
			int dis[] = getSetofDistances(y);
			int chord[] = new int[4];
			chord = getChordfromDistances(dis, y);
			
			int eval[] = evaluarAcorde(dis, y);
			int d = calcularDistancia(dis);
			
			System.out.print("[");
			System.out.print(getText(chord));
			System.out.print("]	");
			
			getModulo(chord);
			
			System.out.print("[");
			System.out.print(getText(chord));
			System.out.print("]	");
			
			System.out.print("(");
			System.out.print(getText(dis));
			System.out.print(")	");

			System.out.print("(");
			System.out.print(getText(eval));
			System.out.print(")  ");
			
			System.out.print(d + "	");
			
			System.out.println(esPosible(eval, d, y));
			
		}
	
	}
	
	//Method to produce an aleatory chord
	static int[] getChord(int y){
		
		int chord[] = new int[4];
		
		for (int i = 0; i < chord.length; i++){
			chord[i] = randomNote(y) + 60;
		}
		
		Arrays.sort(chord);

		return chord;
	
	}
	
	//Method to produce an aleatory note
	static int randomNote(int y){
		
		int r1 = r.nextInt(32 - y);
		
		if (r1 < (8 - y)){
			int r2 = r.nextInt(26 + y);
			r2 = r2 + 8;
			if (r1 < r2){
				r1 = r2;
			}
		}
		
		return r1;
		
	}
	
	//Method to get a set of 4 distances to get a chord.
	static int[] getSetofDistances(int y){
	
		int d[] = new int[4];
		
		for (int i = 0; i < d.length; i++){
			d[i] = randomDistance(y);
		}
		
		Arrays.sort(d);

		return d;
	
	}
	
	//Method to produce a random distance from de nut.
	static int randomDistance(int y){
	
		int d = r.nextInt(12 - y);
		d = d - 1;
		
		return d;
	
	}
	
	//Method that receives a set of distances and return the chord.
	static int[] getChordfromDistances(int[] d, int y){
	
		int chord[] = new int[d.length];
		
		for (int i = 0; i < d.length; i++){
			chord[i] = d[i] + getAjuste(y) + i*7;
		}
	
		return chord;
		
	}
	
	//Method to obtain distances from de nut
	static int[] getDistances(int[] x, int y){
	
		int distancias[] = new int[x.length];
		
		for (int i = 0; i < x.length; i++){
			distancias[i] = x[i] - getAjuste(y) - i*7;
		}
	
		return distancias;
	}
	
	
	/*Method to evaluate chord feasibility. Returns a value for each note:
	0. open strings, 1. Normal stop, 2. Difficult stop, 3. Very difficult or
	imposible stop - note out of register*/
	static int[] evaluarAcorde(int x[], int y){
		
		int instrumentoElegido = y - 1;
		int factibilidad[] = new int [4];
		int cejilla = 0;
		boolean existeCejilla = false;
		
		for (int cuerda = 0; cuerda < 4; cuerda ++){
		
		if (x[cuerda] < 0){
			factibilidad[cuerda] = 3;
		} else if (x[cuerda] == 0){
			factibilidad[cuerda] = 0;
		} else {
			factibilidad[cuerda] = 1;
		}
		
		if (cuerda == 1){
			
			if (x[1] == x[0] && x[1] > 0){
				if (instrumentoElegido == 2){
					existeCejilla = true;
				}
				factibilidad[0] = 2;
				factibilidad[1] = 2;				
			}
				
		}
		
		if (cuerda == 2){
		
			if (x[2] == x[1] && x[1] == x[0] && x[2] > 0){
				if (instrumentoElegido == 2){
					factibilidad[2] = 2;
					if (existeCejilla == false){
						existeCejilla = true;
					}	
				} else {
					factibilidad[0] = 3;
					factibilidad[1] = 3;
					factibilidad[2] = 3;	
				}
			}
			
			if (x[2] == x[0] && x[2] != x[1] && x[2] > 0){
				if (instrumentoElegido == 2){
					factibilidad[0] = 2;
					if (x[1] < x[2]  && x[1] > 0){
						factibilidad[1] = 3;
					}
					factibilidad[2] = 2;
					if (existeCejilla == false){
						existeCejilla = true;
					}
				} else {
					factibilidad[0] = 3;
					factibilidad[2] = 3;	
				}
			}
			
			if (x[2] == x[1] && x[2] != x[0] && x[2] > 0){
				if (instrumentoElegido == 2){
					factibilidad[1] = 2;
					if (x[0] < x[2]  && x[0] > 0){
						factibilidad[0] = 3;
					}
					factibilidad[2] = 2;	
					if (existeCejilla == false){
						existeCejilla = true;
						cejilla = 1;
					}
				} else {
					factibilidad[1] = 2;
					factibilidad[2] = 2;	
				}
			}
			
			if (x[0] == x[1] && x[2] < x[0] && x[2] > 0){
				if (instrumentoElegido == 2){
					factibilidad[2] = 3;
				}
			}
		
		}
		
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
							factibilidad[0] = 3;
							factibilidad[3] = 3;
						} else {
							factibilidad[0] = 2;
							factibilidad[1] = 3;
							factibilidad[2] = 3;
							factibilidad[3] = 2;
						}
						cejilla = 0;
					} else {
						factibilidad[0] = 2;
						if (x[1] < x[3]  && x[1] > 0){
							factibilidad[1] = 3;
						}
						if (x[2] < x[3]  && x[2] > 0){
							factibilidad[2] = 3;
						}
						factibilidad[3] = 2;	
						existeCejilla = true;
					}
				} else {
					if (x[3] != x[2]){
						factibilidad[0] = 3;
					}
					if (x[1] == x[3]){
						factibilidad[1] = 3;
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
							factibilidad[1] = 3;
							factibilidad[3] = 3;
						} else {
							factibilidad[1] = 2;
							factibilidad[0] = 3;
							factibilidad[2] = 3;
							factibilidad[3] = 2;
						}
						cejilla = 1;
					} else {
						factibilidad[1] = 2;
						if (x[0] < x[3]  && x[0] > 0){
							factibilidad[0] = 3;
						}
						if (x[2] < x[3]  && x[2] > 0){
							factibilidad[2] = 3;
						}
						factibilidad[3] = 2;
						existeCejilla = true;
						cejilla = 1;
					}
				} else {
					factibilidad[1] = 3;
					if (x[3] == x[0] && factibilidad[0] != 3){
						factibilidad[0] = 3;
					}
					if (x[3] == x[2] && factibilidad[2] != 3){
						factibilidad[2] = 3;
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
							factibilidad[2] = 3;
							factibilidad[3] = 3;
						} else {
							factibilidad[0] = 3;
							factibilidad[1] = 3;
							factibilidad[2] = 2;
							factibilidad[3] = 2;
						}
						cejilla = 2;
					} else {
						factibilidad[2] = 2;
						if (x[0] < x[3]  && x[0] > 0){
							factibilidad[0] = 3;
						}
						if (x[1] < x[3]  && x[1] > 0){
							factibilidad[1] = 3;
						}
						factibilidad[3] = 2;
						existeCejilla = true;
						cejilla = 2;
					}
				} else {
					if (x[0] != x[3] && x[1] != x[3]){
						factibilidad[2] = 2;
						factibilidad[3] = 2;
					}
					if (x[0] == x[3] && x[1] != x[3]){
						factibilidad[3] = 3;						
					}
					if (x[1] == x[3] && x[0] != x[3]){
						factibilidad[1] = 3;
						factibilidad[2] = 3;
						factibilidad[3] = 3;						
					}
				}	
						
			}
			
			if (existeCejilla == true && x[3] < x[cejilla] && x[3] > 0 && x[3]!= x[cejilla]){
				factibilidad[3] = 3;
			}
			
			}
			
		}
		
		return factibilidad;
		
	}
	
	//Method to obtain the distance to be cover by player's hand
	static int calcularDistancia (int x[]){
	
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
	
	//Method to obtain note code (0-11)
	static void getModulo(int[] c){
		
		for (int i = 0; i < c.length; i++){
			if (c[i] < 0){
				c[i] = c[i]*(-1);
				while (c[i] > 11){ 
				c[i] = c[i] - 12;
				}
				c[i] = 12 - c[i];
			} else {
				while (c[i] > 11){ 
					c[i] = c[i] - 12;
				}
			}
		}
	
	}
	
	//Method that returns a constant for adjusting values for selected instrument
	static int getAjuste(int x){
		
		int ajuste = 36;
		if (x == 1){
			ajuste = ajuste + 19;
		} else if (x == 2) {
			ajuste = ajuste + 12;		
		} 
		
		return ajuste;
	
	}
	
	/*Method to decide if a chord if posible. Recieves array of distances,
	distance to cover and instrument to consider.*/
	static String esPosible(int[] x, int y, int z){
	
		boolean p = true;
		String f = "";
		
		for (int i = 0; i < x.length; i++){
			if (x[i] < 0 || x[i] == 3){
				p = false;
				break;
			}
		}
		
		if (p == true){
			if (y > 7 - z){
				p = false;
			}
		}
		
		if (p == false){
			f = "NO";
		} else if (p == true){
			f = "YES";
		}
		
		return f;
	
	}
	
	//Method to get strings from arrays of integers
	static String getText(int[] x){
		
		String conjuntoAlturas = new String();

		for (int i=0; i < x.length ; i++){	
			conjuntoAlturas += String.valueOf(x[i]);
			if (i < x.length - 1){
				conjuntoAlturas += " ";
			}
		}
		
		return conjuntoAlturas;
	
	}
	
	//Method for exit the program...
	public static void exit () throws Exception{
		
		InputStreamReader isr2 = new InputStreamReader(System.in);
		BufferedReader br2 = new BufferedReader(isr2);

		System.out.println();
		System.out.println("To run program again: 'n'");
		String ex;
		ex = br2.readLine();
		if (ex.equals("n")) {
			System.out.println(); 
			System.out.println(); 			
			ChordGenerator();
		} else {
			System.exit(0);
		}
	
	}

}