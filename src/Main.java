import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class Main {
    private static final int ROZMIAR_POPULACJI = 10;
    private static final int WARUNEK_STOPU = 50000;
    private static final DecimalFormat DF = new DecimalFormat("0.000", new DecimalFormatSymbols(new Locale("pl", "PL")));// warunek stopu = ilość stworzonych populacji
    static Random rand = new Random();
    public static void main(String[] args) throws FileNotFoundException {
        long start = System.currentTimeMillis();
        String plikWe = "gr24_matrix.txt"; // <- nazwa pliku wejściowego
        int[][] odleglosci = wczytajPlik(plikWe);
        int[] sumBestGlobal = new int[(ROZMIAR_POPULACJI*WARUNEK_STOPU)];
        int[] sumBestCurrent = new int[(ROZMIAR_POPULACJI*WARUNEK_STOPU)];

        for(int i = 0; i < 30; i++) {
            // Generowanie losowej populacji
            List<int[]> populacja = generujPopulacje(odleglosci, ROZMIAR_POPULACJI);

            int[] bestGlobal = populacja.getFirst();
            int[] bestCurrent = populacja.getFirst();

            for (int element = 0; element < ROZMIAR_POPULACJI; element++)
            {
                if (funkcjaDopasowanie(populacja.get(element), odleglosci) < funkcjaDopasowanie(bestCurrent, odleglosci)) {
                    bestCurrent = populacja.get(element);
                    bestGlobal = bestCurrent;
                }
                sumBestCurrent[element] += funkcjaDopasowanie(bestCurrent,odleglosci);
                sumBestGlobal[element] += funkcjaDopasowanie(bestGlobal,odleglosci);
            }
            // Warunek stopu
            for (int pokolenie = 1; pokolenie <= WARUNEK_STOPU-1; pokolenie++) {
                int ROZMIAR_TURNIEJU = 10;
                //Krzyzowanie i Mutacja
                List<int[]> poKrzyzowaniu = new ArrayList<>();
                bestCurrent = populacja.getFirst();

                for (int x = 1; x <= ROZMIAR_POPULACJI; x++) {
                    int[][] potomkowie = krzyzowaniePMX(turniej(populacja, odleglosci, ROZMIAR_TURNIEJU), turniej(populacja, odleglosci, ROZMIAR_TURNIEJU));
                    for (int[] ints : potomkowie) {
                        poKrzyzowaniu.add((rand.nextDouble() <= 0.01) ? mutacja(ints) : ints);
                    }
                    for (int[] ints : poKrzyzowaniu) {
                        if (funkcjaDopasowanie(ints, odleglosci) < funkcjaDopasowanie(bestCurrent, odleglosci)) {
                            bestCurrent = ints;
                        }
                    }
                    if (funkcjaDopasowanie(bestCurrent, odleglosci) < funkcjaDopasowanie(bestGlobal, odleglosci)) {
                        bestGlobal = bestCurrent;
                    }
                    sumBestCurrent[(x+(ROZMIAR_POPULACJI*(pokolenie)))-1] += funkcjaDopasowanie(bestCurrent,odleglosci);
                    sumBestGlobal[(x+(ROZMIAR_POPULACJI*(pokolenie)))-1] += funkcjaDopasowanie(bestGlobal,odleglosci);
                }

                populacja.clear();
                populacja = poKrzyzowaniu;
            }
            populacja.clear();
        }
        eksportujWynikiWykresu(sumBestGlobal,sumBestCurrent);
        System.out.printf("\nCzas 30 wykonań %.3f s",((float)(System.currentTimeMillis()-start)/60));
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
    private static void eksportujWynikiWykresu(int[] sumBestGlobal, int[]sumBestCurrent) throws FileNotFoundException{
        PrintWriter wynikWriter = new PrintWriter("wynik.txt");
        for (int pok = 0; pok < sumBestGlobal.length; pok++) {
            double avgCurr = (double) sumBestCurrent[pok] / 30;
            double avgGlob = (double) sumBestGlobal[pok] / 30;
            wynikWriter.println((pok+1) + ";" + DF.format(avgCurr) + ";" + DF.format(avgGlob));
        }
        wynikWriter.close();
    }
    private static List<int[]> generujPopulacje(int[][] odleglosci ,int rozmiar) {
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
    private static boolean zawiera(int x, int[] tab) {
        for (int j : tab) {
            if (j == x) {
                return true;
            }
        }
        return false;
    }
    private static int funkcjaDopasowanie(int[] element, int[][] odleglosci) {
        int dop = 0;
        for(int j = 0; j< odleglosci.length; j++){
            dop += odleglosci[element[j]][element[j+1]];
        }
        return dop;
    }
    private static int[][] krzyzowaniePMX(int[] rodzic1, int[] rodzic2) {
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
    private static int powtorzenie(int[] tab){
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
    private static int[] mutacja(int[] droga) {
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
                if (funkcjaDopasowanie(ints, odleglosci) < funkcjaDopasowanie(najlepszy, odleglosci)) {
                    najlepszy = ints;
                }
            }
        return najlepszy;
    }
}
