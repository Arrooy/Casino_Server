package Model.HorseRace_Model;

import java.io.Serializable;
import java.util.Random;

public class HorseSchedule implements Serializable {
    private int[][] times;
    private int winner;



    private static final int MAX_HORSES = 12;
    private static final int SECTIONS = 5;

    private static final int MAX_SECTION_TIME = 200;
    private static final int MIN_SECTION_TIME = 100;



    public HorseSchedule(){
        Random rand = new Random();
        int[] totalTime = new int[12];
        int bestTime = 1300;
        this.winner = -1;
        this.times = new int[MAX_HORSES][SECTIONS];

        for(int i = 0; i < MAX_HORSES; i++){
            for(int j = 0; j < SECTIONS; j++){
                this.times[i][j] = rand.nextInt(100) + 100;
                totalTime[i]+=times[i][j];
            }
            if(totalTime[i] < bestTime){
                bestTime = totalTime[i];
                this.winner = i;
            }
        }
    }



    public int getRaceTime(){
        int slowestTime = -1;
        int[] totalTime = new int[12];

        for(int i = 0; i < 12; i++){
            for(int j = 0; j < 5; j++){
                totalTime[i]+=times[i][j];
            }
            if(totalTime[i] > slowestTime){
                slowestTime = totalTime[i];
            }
        }
        return slowestTime;
    }


    public int getTime(int i,int j){
        return times[i][j];
    }


    public int getWinner(){
        return winner;
    }
}
