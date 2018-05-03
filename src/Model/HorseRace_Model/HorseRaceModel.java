package Model.HorseRace_Model;

import Model.User;

import java.util.ArrayList;
import java.util.Random;

public class HorseRaceModel {
    private HorseSchedule horseSchedule;
    private ArrayList<HorseBet> pendingBets;

    private static final int MAX_HORSES = 12;
    private static final int SECTIONS = 5;

    public HorseRaceModel (){
     this.pendingBets = new ArrayList<>();
    }

    public HorseSchedule getHorseSchedule(){
        return this.horseSchedule;
    }
    public void addBet(HorseBet horseBet){
        this.pendingBets.add(horseBet);
    }

    public ArrayList<HorseBet> getPendingBets(){
        return pendingBets;
    }

    public void setHorseSchedule(HorseSchedule horseSchedule){
        this.horseSchedule = horseSchedule;
    }
}
