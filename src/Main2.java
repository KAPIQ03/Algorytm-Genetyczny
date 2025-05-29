import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Main2 {
    private static final int ROZMIAR_POPULACJI = 100;
    private static final int WARUNEK_STOPU = 10;
    static Random rand = new Random();
    public static void main(String[] args) throws FileNotFoundException {
        long start = System.currentTimeMillis();
        String plikWe = "input.txt";
        String plikWy = "out.txt";

        int[][] odleglosci = wczytajPlik(plikWe);

        // wyświetlanie tablicy odległości
        for (int[] ints : odleglosci) {
            for (int j = 0; j < odleglosci.length; j++) {
                System.out.print(ints[j] + " ");
            }
            System.out.print("\n");
        }

        // Generowanie losowej populacji
        List<int[]> populacja = generujPopulacje(odleglosci,ROZMIAR_POPULACJI);

        // Warunek stopu
        for (int x = 0; x < WARUNEK_STOPU; x++) {
            int ROZMIAR_TURNIEJU = 10;
            //Krzyzowanie i Mutacja
            List<int[]> poKrzyzowaniu = new ArrayList<>();
            for (int i = 0; i < ROZMIAR_POPULACJI; i++) {
                int[][] potomkowie = krzyzowaniePMX(turniej(populacja,odleglosci,ROZMIAR_TURNIEJU), turniej(populacja,odleglosci,ROZMIAR_TURNIEJU));
                for (int[] ints : potomkowie) {
                    poKrzyzowaniu.add((rand.nextDouble() <= 0.1) ? mutacja(ints) : ints);
                }
            }

//            System.out.println("Po Krzyzowaniu");
//
//            for (int[] ints : poKrzyzowaniu) {
//                for (int j = 0; j < odleglosci.length + 1; j++) {
//                    System.out.print(ints[j] + ", ");
//                }
//                System.out.printf("%.3f", funkcjaDopasowanie(ints, odleglosci));
//                System.out.print("\n");
//            }

            populacja.clear();
            populacja = poKrzyzowaniu;
        }

        //wybranie najlepszego osobnika z wyjsciowej po krzyrzowaniu populacji
        int[] najlepszy = populacja.getFirst();
        for (int[] ints : populacja) {
            if(funkcjaDopasowanie(ints,odleglosci)>funkcjaDopasowanie(najlepszy, odleglosci)) {
                najlepszy = ints;
            }
        }

        // wyświetlanie wyniku;
        System.out.println("\n\n---WYNIK---");
        System.out.println("liczba miast do odwiedzenia = "+(najlepszy.length-1));
        System.out.print("kolejność: ");
        for (int j : najlepszy) {
            System.out.print((j + 1) + ", ");
        }
        int suma = 0;
        for(int i = 0; i < odleglosci.length; i++) {
            suma += odleglosci[najlepszy[i]][najlepszy[i+1]];
        }
        System.out.println("\nSuma: " + suma);

        System.out.print("odległości: ");
        for(int i = 0; i < odleglosci.length; i++) {
            System.out.print( odleglosci[najlepszy[i]][najlepszy[i+1]]+", ");
        }
        System.out.print("\nodległość narastająco: ");
        suma = 0;
        for(int i = 0; i < odleglosci.length; i++) {
            System.out.print(suma+", ");
            suma += odleglosci[najlepszy[i]][najlepszy[i+1]];
        }
        System.out.print(suma+"\n");

        //eksport danych do pliku
        eksportujDopliku(najlepszy,odleglosci,plikWy);

        // wyświetlanie czasu pracy algorytmu
        System.out.printf("\n%.3f s",((float)(System.currentTimeMillis()-start)/60));
    }
    private static int[][] wczytajPlik(String nazwaPliku) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(nazwaPliku));
        int N = sc.nextInt();
        int[][] dist = new int[N][N];
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                dist[i][j] = sc.nextInt();
        sc.close();
        return dist;
    }
    private static void eksportujDopliku(int[] najlepszy, int[][] odleglosci, String nazwaPliku) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(nazwaPliku);
        //wypisanie ilości miast
        out.print(najlepszy.length-1 + "; ");
        int suma = 0;
        //wypisanie kolejności odwiedzania miast
        for(int j: najlepszy) {
            out.print(j + 1 + "; ");
        }
        //wypisanie długości cyklu
        for(int i = 0; i < odleglosci.length; i++) {
            suma += odleglosci[najlepszy[i]][najlepszy[i+1]];
        }
        out.print(suma+"; ");
        //wypisanie odległości między miastami
        for(int i = 0; i < odleglosci.length; i++) {
            out.print( odleglosci[najlepszy[i]][najlepszy[i+1]]+"; ");
        }
        //wypisanie odległości przebytej przez komiwojazera
        suma = 0;
        for(int i = 0; i < odleglosci.length; i++) {
            out.print(suma+"; ");
            suma += odleglosci[najlepszy[i]][najlepszy[i+1]];
        }
        out.print(suma+"; ");
        out.close();
    }
    public static List<int[]> generujPopulacje(int[][] odleglosci ,int rozmiar) {
        List<int[]> populacja = new ArrayList<>();
        for (int i = 0; i < rozmiar; i++) {
            int[] trasa = new int [odleglosci.length+1];
            Arrays.fill(trasa, -1);
            int j = 0;

            while (j<odleglosci.length) {
                int x = rand.nextInt(odleglosci.length);
                if (!zawiera(x,trasa)) {
                    trasa[j] = x;
                    j++;
                }
            }
            trasa[odleglosci.length] = trasa[0];
            populacja.add(trasa);
        }
        return populacja;
    }
    public static boolean zawiera(int x, int[] tab) {
        for (int j : tab) {
            if (j == x) {
                return true;
            }
        }
        return false;
    }
    public static double funkcjaDopasowanie(int[] element, int[][] odleglosci) {
        double dop = 0;
        for(int j = 0; j< odleglosci.length; j++){
            dop += odleglosci[element[j]][element[j+1]];
        }
        return (1/dop)*1000000;
    }
    public static int[][] krzyzowaniePMX(int[] rodzic1, int[] rodzic2) {
        int[][] potomkowie = new int[2][rodzic1.length];

        int n = rodzic1.length;
        int[] potomek1 = new int[n];
        int[] potomek2 = new int[n];
        Arrays.fill(potomek1, -1);
        Arrays.fill(potomek2, -1);

        int p1 = rand.nextInt(1,n - 1);
        int p2 = rand.nextInt(1,n - 1);
        while(p1==p2){
            p2=rand.nextInt(1,n - 1);
        }
        if(p1>p2){
            int pom = p1;
            p1 = p2;
            p2= pom;
        }
        if (p2 + 1 - p1 >= 0) System.arraycopy(rodzic1, p1, potomek1, p1, p2 + 1 - p1);
        if (p2 + 1 - p1 >= 0) System.arraycopy(rodzic2, p1, potomek2, p1, p2 + 1 - p1);

        for (int i = 0; i < p1; i++) {
            potomek1[i] = rodzic2[i];
            potomek2[i] = rodzic1[i];
        }
        for (int i = p2+1; i < rodzic1.length-1; i++) {
            potomek2[i] = rodzic1[i];
            potomek1[i] = rodzic2[i];
        }
        potomek1[potomek1.length-1] = rodzic2[potomek2.length-1];
        potomek2[potomek2.length-1] = rodzic1[potomek1.length-1];

        while(powtorzenie(potomek1)>-1){
            int pow1 = powtorzenie(potomek1);
            int pow2 = powtorzenie(potomek2);
            potomek1[indexOfpow(potomek1,pow1)] =  pow2;
            potomek2[indexOfpow(potomek2,pow2)] = pow1;
        }
        potomkowie[0] = potomek1;
        potomkowie[1] = potomek2;

        return potomkowie;
    }
    public static int powtorzenie(int[] tab){
        for (int k = 0; k < tab.length-1; k++) {
            int pom = 0;
            for (int i = 0; i < tab.length-1; i++) {
                if (tab[k] == tab[i]) {
                    pom++;
                }
                if (pom >= 2) {
                    return tab[k];
                }
            }
        }
        return -1;
    }
    public static int[] mutacja(int[] droga) {
        int[] value = droga.clone();
        int x = rand.nextInt(1,value.length-1);
        int y = rand.nextInt(1,value.length-1);
        while(x==y){
            y = rand.nextInt(1,value.length-1);
        }
        int pom = value[x];
        value[x] = value[y];
        value[y] = pom;
        return value;
    }
    private static int indexOfpow(int[] tab, int wartosc) {
        int pom = 0;
        for (int i = 0; i < tab.length-1; i++) {
            if (tab[i] == wartosc) pom++;
            if(pom>=2){
                return i;
            }
        }
        return -1;
    }
    private static int[] turniej(List<int[]>populacja, int[][] odleglosci,int rozmiarTurnieju){
        List<int[]> turniej = new ArrayList<>();
        for (int j = 0; j < rozmiarTurnieju; j++) {
            turniej.add(populacja.get(rand.nextInt(populacja.size())));
        }
        int[] najlepszy = turniej.getFirst();
        for (int[] ints : turniej) {
            if (funkcjaDopasowanie(ints, odleglosci) > funkcjaDopasowanie(najlepszy, odleglosci)) {
                najlepszy = ints;
            }
        }
        return najlepszy;
    }
}