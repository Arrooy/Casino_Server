package Model.HorseRace_Model;


import java.util.ArrayList;


/**Model de la cursa de cavalls*/
public class HorseRaceModel {
    /**Temps de cada cavall a cada seccio*/
    private HorseSchedule horseSchedule;
    /**Apostes que queden per gestionar*/
    private ArrayList<HorseBet> pendingBets;

    public HorseRaceModel (){
     this.pendingBets = new ArrayList<>();
    }

    /**
     * Retorna una cursa amb el temps per seccio de cada cavall
     * @return cursa amb el temps per seccio de cada cavall
     */
    public HorseSchedule getHorseSchedule(){
        return this.horseSchedule;
    }

    /**
     * Afageix una aposta a la llista d'apostes pendents
     * @param horseBet aposta a afegir
     */
    public void addBet(HorseBet horseBet){
        this.pendingBets.add(horseBet);
    }

    /**
     * Retorna la llista d'apostes pendents
     * @return llista d'apostes pendents
     */
    public ArrayList<HorseBet> getPendingBets(){
        return pendingBets;
    }

    /**
     * Permet afegir una nova cursa amb el temps per seccio de cada cavall
     * @param horseSchedule cursa amb el temps per seccio de cada cavall
     */
    public void setHorseSchedule(HorseSchedule horseSchedule){
        this.horseSchedule = horseSchedule;
    }
}
