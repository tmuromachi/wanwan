import be.tarsos.dsp.util.PitchConverter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class wanwanPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = -5330666476785715988L;
    private double patternLength;//in seconds
    private double currentMarker;
    private int score;
    private double patternLengthInQuarterNotes;

    private static final double CENTS_DEVIATION = 30.0;

    double[] pattern={400,400,600,400,900,200,400,400,600,400,1100,900}; // in cents
    double[] timing ={3  ,1  ,4  ,4  ,4  ,2  ,3  ,1  ,4  ,4  ,4  ,6   }; //in eight notes

    ArrayList<Double> startTimeStamps;
    ArrayList<Double> pitches;

    private boolean silentSection = false;
    ArrayList<Double> silentSectionTimes;
    ArrayList<Double> silentSectionPitches;
    private boolean resetFlag = false;
    private long resetTime = 0;
    private int recentResetRepeat = 0;
    private int currentResetRepeat = 0;

    public wanwanPanel(){
        for(double timeInQuarterNotes : timing){
            patternLengthInQuarterNotes+=timeInQuarterNotes;
        }
        patternLength = 12;
        currentMarker = 0;
        startTimeStamps = new ArrayList<Double>();
        pitches = new ArrayList<Double>();
        silentSectionTimes = new ArrayList<Double>();
        silentSectionPitches = new ArrayList<Double>();
    }

    @Override
    public void paint(final Graphics g) {
        final Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setBackground(Color.GRAY);
        graphics.clearRect(0, 0, getWidth(), getHeight());
        int x = (int) (currentMarker / (float) patternLength * getWidth());

        //現在バー描画
        //描画更新ができない不具合解決のためフラグで管理 現在の繰り返す数と前回までに繰り返した数を比較し、違っていたら描画更新
        setResetTime(getResetTime() + 1);
        if(getCurrentResetRepeat() != getRecentResetRepeat()) {
            setResetFlag(true);
        }

        if(getResetFlag() == true) {
            setRecentResetRepeat(getCurrentResetRepeat());
            //score();
            pitches.clear();
            startTimeStamps.clear();
            silentSectionTimes.clear();
            setResetFlag(false);
        }
        graphics.drawLine(x, 0, x, getHeight());


        //ノード描画

        //ピッチプロット
        //graphics.setColor(Color.RED);
        for(int i = 0 ; i < pitches.size() ; i++){
            double pitchInCents = pitches.get(i);
            double startTimeStamp = startTimeStamps.get(i) % patternLength;
            int patternX = (int) ( startTimeStamp / (double) patternLength * getWidth());
            int patternY = getHeight() - (int) (pitchInCents / 1500.0 * getHeight());
            graphics.setColor(Color.RED);
            if (i == (pitches.size() -1)) {
                graphics.setColor(Color.YELLOW);
            }
            graphics.fillRoundRect(patternX, patternY, 10,10,10 ,10);

            //無音区間追加
            if(i == (pitches.size()-1)) {
                if(getSilentSection() == true) {
                    silentSectionTimes.add(startTimeStamp);
                    silentSectionPitches.add(pitches.get(i));
                    setSilentSection(false);
                }
            }
        }
        //無音区間プロット
        for(int i = 0 ; i < silentSectionTimes.size() ; i++) {
            double startTimeStamp = silentSectionTimes.get(i) % patternLength;
            int silentX = (int) ( startTimeStamp / (double) patternLength * getWidth());
            graphics.setColor(Color.BLUE);
            graphics.fillRect(silentX, 50, 20,getHeight()-100);

            //無音区間ピッチプロット
            graphics.setColor(Color.ORANGE);
            double pitchInCents = silentSectionPitches.get(i);
            int silentY = getHeight() - (int) (pitchInCents / 1500.0 * getHeight());
            graphics.fillRoundRect(silentX, silentY, 20,20,20 ,20);
            System.out.println(silentSectionTimes.size());
        }
        //五線を書く
        graphics.setColor(Color.WHITE);
        for (int i = 0; i < 12; i++) {
            g.drawLine(0, 100 + i*70, 1980, 100 + i*70);
        }

    }

//    private void score(){
//        score = 0;
//        for(int i = 0 ; i < pitches.size() ; i++){
//            double pitchInCents = pitches.get(i);
//            double startTimeStamp = startTimeStamps.get(i) % patternLength;
//            if(startTimeStamp > 0.5 && startTimeStamp <= 0.5 + 0.5 * pattern.length){
//                double lengthPerQuarterNote = patternLength/patternLengthInQuarterNotes; // in seconds per quarter note
//                double currentXPosition = 0.5; // seconds of pause before start
//                for(int j = 0 ; j < pattern.length ; j++){
//                    double lengthInSeconds = timing[j] * lengthPerQuarterNote;//seconds
//                    if(startTimeStamp > currentXPosition && startTimeStamp <= currentXPosition + lengthInSeconds && Math.abs(pitchInCents-pattern[j]) < CENTS_DEVIATION){
//                        score++;
//                    }
//                    currentXPosition += lengthInSeconds; //in seconds
//                }
//            }
//        }
//    }

    /**
     * 周波数を図にプロットする。
     * @param timeStamp
     * @param frequency
     */
    public void setMarker(double timeStamp,double frequency) {
        currentMarker = timeStamp % patternLength;
        setCurrentResetRepeat((int)(timeStamp / patternLength));
        //ignore everything outside 80-2000Hz
        if (frequency > 80 && frequency < 2000) {
            double pitchInCents = PitchConverter.hertzToRelativeCent(frequency);
            pitches.add(pitchInCents);
            startTimeStamps.add(timeStamp);
        }


        this.repaint();
    }

    public boolean getSilentSection() { return this.silentSection; }
    public void setSilentSection(boolean silentSection) { this.silentSection = silentSection; }

    public boolean getResetFlag() { return this.resetFlag; }
    public void setResetFlag(boolean resetFlag) { this.resetFlag = resetFlag; }

    public long getResetTime() { return this.resetTime; }
    public void setResetTime(long resetTime) { this.resetTime = resetTime; }

    public int getRecentResetRepeat() { return this.recentResetRepeat; }
    public void setRecentResetRepeat(int recentResetRepeat) { this.recentResetRepeat = recentResetRepeat; }

    public int getCurrentResetRepeat() { return this.currentResetRepeat; }
    public void setCurrentResetRepeat(int currentResetRepeat) { this.currentResetRepeat = currentResetRepeat; }
}