package hc.utils;

import java.util.Random;

/**
 * Class responsible for providing wait timers instance-wide
 */
public class Timer {
    private Random generator; // thread-safe random generator
    private int evalMin; // inclusive value
    private int evalMax; // exclusive value
    private int appointMin;
    private int appointMax;
    private int payMin;
    private int payMax;
    private int moveMin;
    private int moveMax;

    public static class Builder {

        private int evalMin = 0;
        private int evalRange = 0;
        private int appointMin = 0;
        private int appointRange = 0;
        private int payMin = 0;
        private int payRange = 0;
        private int moveMin = 0;
        private int moveRange = 0;

        public Builder() {
        }

        public Builder withMinimumEvaluationTime(int time) {
            this.evalMin = time;
            return this;
        }

        public Builder withEvaluationTimeRange(int time) {
            this.evalRange = time;
            return this;
        }

        public Builder withMinimumAppointmentTime(int time) {
            this.appointMin = time;
            return this;
        }

        public Builder withAppointmentTimeRange(int time) {
            this.appointRange = time;
            return this;
        }

        public Builder withMinimumPaymentTime(int time) {
            this.payMin = time;
            return this;
        }

        public Builder withPaymentTimeRange(int time) {
            this.payRange = time;
            return this;
        }

        public Builder withMinimumMovementTime(int time) {
            this.moveMin = time;
            return this;
        }

        public Builder withMovementTimeRange(int time) {
            this.moveRange = time;
            return this;
        }

        public Timer build() {
            Timer timer = new Timer();
            timer.evalMin = this.evalMin;
            timer.evalMax = this.evalMin + this.evalRange + 1;
            timer.appointMin = this.appointMin;
            timer.appointMax = this.appointMin + this.appointRange + 1;
            timer.payMin = this.payMin;
            timer.payMax = this.payMin + this.payRange + 1;
            timer.moveMin = this.moveMin;
            timer.moveMax = this.moveMin + this.moveRange + 1;

            return timer;
        }
    }

    private Timer() {
        generator = new Random();
    }

    public int getEvaluationTime() {
        return generator.nextInt(evalMin, evalMax);
    }

    public int getAppointmentTime() {
        return generator.nextInt(appointMin, appointMax);
    }

    public int getPaymentTime() {
        return generator.nextInt(payMin, payMax);
    }

    public int getMovementTime() {
        return generator.nextInt(moveMin, moveMax);
    }
}
