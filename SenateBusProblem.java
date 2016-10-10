package ConcurrentLab3;
/*
 * author Yasara, Thamali
 * "Little Book of samaphores" solution number 2 which has scheduled for
 * given requirenment that busses and riders will continue to arrive throughout the day. 
 * Assume inter-arrival time of busses and riders are exponentially distributed with
 *  a mean of 20 min and 30 sec, respectively.  
 */
import java.util.Random;
import java.util.concurrent.Semaphore;

public class SenateBusProblem {
	// Since this is through out per day
	private static final int MaxBusCapacity = 50;
	private static final int BusesPerDayCount = 5;
	private static final int PassengerPerDayCount = 25;
	static int waiting = 0;
	static Semaphore mutex = new Semaphore(1);
	static Semaphore bus = new Semaphore(0);
	static Semaphore boarded = new Semaphore(0);
	//static boolean isBusBoarded = false;

	class Bus implements Runnable {
		private int n;
		private int busIndex;
		private int noOfRiders = 0;

		Bus(int index) {
			this.busIndex = index;
		}

		@Override
		public void run() {
			try {
				mutex.acquire();
				System.out
						.println("[Bus get the mutex]Bus " + busIndex + " came to the bustop ..");
				
				n = Math.min(waiting, MaxBusCapacity);
				for (int i = 0; i < n; i++) {
					bus.release();
					boarded.acquire();
					noOfRiders++;
				    //new Thread(this).sleep(800);//by assuming one passenger need 500 milisecond to get up the bus because to test the passenger come while boarding the bus and whether he has wait for next bus(to keep bus boarded for long time)
				}
				
				waiting = Math.max((waiting - MaxBusCapacity), 0);
				System.out.println("[Bus going to release the mutex]Bus " + busIndex + " is leaving the busstop...");
				mutex.release();
				
			} catch (InterruptedException ex) {
				System.err.print("Bus " + busIndex
						+ "'s thread got interrupted :( " + ex.getMessage());
				mutex.release();//Since if one bus has break down other bus should be run without intteruption of process throughout the day
				boarded.release();

			}
			depart();
		}

		public void depart() {
			//isBusBoarded = false;
			System.out.println("Bus " + busIndex + " left the bus stop with "
					+ noOfRiders + " riders");
			new Thread(this).stop();
		}
	}

	class Rider implements Runnable {

		private int riderIndex;

		Rider(int index) {
			this.riderIndex = index;
		}

		@Override
		public void run() {
			try {
				if (mutex.availablePermits()==0)
					System.out.println("[mutex had accquired by bus]Bus is boarding, so passenger "
							+ riderIndex + " has to waite for next bus !!");
				mutex.acquire();
				System.out.println("[mutex accuried by rider] Rider " + riderIndex + " is waiting !!");
				waiting += 1;
				mutex.release();
				bus.acquire();
				board();
				boarded.release();
			} catch (InterruptedException ex) {
				System.err.print("Rider" + riderIndex
						+ "'s thread got interrupted :( " + ex.getMessage());
				mutex.release();
				boarded.release();
			}
		}

		public void board() {
			System.out.println("Rider " + riderIndex + " is boarded.");
			new Thread(this).stop();
		}
	}

	// Assume inter-arrival time of busses and riders are exponentially
	// distributed with a mean of 20 min and 30 sec, respectively.
	public Double getNextTime(double lambda) {
		Random rand = new Random();
		return new Double(Math.log(1 - rand.nextDouble()) / (-lambda));
	}

	public void SenetorBusDepo() {
		int controlBus = 0;
		int controlRider = 0;
		int busCount = 0;
		int riderCount = 0;
		int nextTimeBus = 0;
		int nextTimeRider = 0;

		int j = 0;
		while (j < PassengerPerDayCount) {
			// A random number generator is used to handle Passengers
			// 1230000.00= 20 min 30 sec in miliseconds
			 nextTimeRider = getNextTime(new Double(1 / 1230000.00)).intValue();
			 //nextTimeRider =1500; //This is for testing since correct time is large
			controlRider = nextTimeRider + controlRider;
			new RiderSchedeler(controlRider, riderCount).start();
			riderCount++;
			j++;
		}
		int i = 0;
		while (i < BusesPerDayCount) {
			// A random number generator is used to handle buses
			// 1230000.00= 20 min 30 sec in miliseconds
			nextTimeBus = getNextTime(new Double(1 / 1230000.00)).intValue();
			//nextTimeBus=5500; //This is for testing since correct time is large
			controlBus = nextTimeBus + controlBus;
			//System.out.println(controlBus);
			new BusSchedeler(controlBus, busCount).start();
			busCount++;

			i++;
		}
	}

	class RiderSchedeler extends Thread {
		int timeDelay = 0;
		int riderCount = 0;

		public RiderSchedeler(int timeDelay, int riderCount) {
			this.timeDelay = timeDelay;
			this.riderCount = riderCount;
		}

		public void run() {

			try {
				Thread.sleep(timeDelay);
			} catch (InterruptedException e) {
				System.out.println(e);
			}
			SendRider(this.riderCount);
			new Thread(this).stop();
		}

		public void SendRider(int ridrCount) {
			Rider tempRidr = new Rider(ridrCount);
			new Thread(tempRidr).start(); // New rider
		}
	}

	class BusSchedeler extends Thread {
		int timeDelay = 0;
		int busCount = 0;

		public BusSchedeler(int timeDelay, int busCount) {
			this.timeDelay = timeDelay;
			this.busCount = busCount;
		}

		public void run() {

			try {
				Thread.sleep(timeDelay);
			} catch (InterruptedException e) {
				System.out.println(e);
			}
			SendBus(this.busCount);
			new Thread(this).stop();
		}

		public void SendBus(int bsCount) {
			Bus tempBus = new Bus(bsCount);
			new Thread(tempBus).start();
		}
	}

	public static void main(String[] args) {
		new SenateBusProblem().SenetorBusDepo();
	}
}
