package sk.jmurin.android.testovac;

/**
 * Created by Janco1 on 26. 5. 2015.
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Janco1
 */
public class Otazka implements Serializable {

    int id;
    String otazka;
    String[] odpovede=new String[8];
    boolean[] jeSpravne=new boolean[8];

    @Override
    public String toString() {
        return "id: "+id+"\n"
                + "otazka: "+otazka+"\n"
                + "odpovede: "+Arrays.toString(odpovede)+"\n"
                + "jeSpravne: "+Arrays.toString(jeSpravne)+"\n";
    }


    public void setSpravne(String spravne) {
        for (int i = 0; i < spravne.length(); i++) {
            int idxOdpovede = (int) (spravne.charAt(i)) - 97;
            jeSpravne[idxOdpovede] = true;
        }
    }
}
