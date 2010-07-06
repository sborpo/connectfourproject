package common;


import java.util.EventObject;
import java.util.Observable;
import java.util.Observer;

public class Timer implements Runnable
{	
	// Rate at which timer is checked in seconds 
	protected int m_rate = 1;
	
	// Length of timeout 
	private int m_length;

	// Time elapsed 
	private int m_elapsed;

	//the pause flag
	private boolean paused;
	
	//the stop flag
	private boolean stopped;
	
	private TimerListener listener;
	
	//private ObservableThing timedOut;
	private boolean timedOut;
	
	//The thread that should run the timer
	private Thread timerThread;
	
	//inner class
	public interface TimerListener 
	{
	    public void timeOutReceived(TimeOutEvent event );
	}
	
	public class TimeOutEvent extends EventObject {
	    private boolean value;
	    
	    public TimeOutEvent( Object source, boolean value ) {
	        super( source );
	        this.value = value;
	    }
	    
	    public boolean getValue() {
	        return value;
	    }
	    
	}
	
	/**
	  * Creates a timer of a specified length
	  * @param	length	Length of time before timeout occurs
	  */
	public Timer ( int lengthInSeconds , TimerListener lst)
	{
		// Assign to member variable
		m_length = lengthInSeconds;
		timedOut = false;
		stopped = false;
		//timedOut.addObserver(obs);
		listener = lst;
		// Set time elapsed
		m_elapsed = 0;
		paused = false;
		timerThread = new Thread(this);
	}

	public Thread getTimerThread(){
		return timerThread;
	}
	
	
	public Timer start()
	{
		timerThread.start();
		return this;
	}
	/** Resets the timer back to zero */
	public synchronized Timer reset()
	{
		m_elapsed = 0;
		timedOut = false;
		return this;
	}

	/** Performs timer specific code */
	public void run()
	{
		// Keep looping
		while(!stopped)
		{
			do{
				// Put the timer to sleep
				try
				{ 
					Thread.sleep(m_rate*1000);
				}
				catch (InterruptedException ioe) 
				{
					return;
				}
			}while(paused);
			// Use 'synchronized' to prevent conflicts
			synchronized ( this )
			{
				// Increment time remaining
				m_elapsed += m_rate;

				// Check to see if the time has been exceeded
				if (m_elapsed > m_length)
				{
					timedOut = true;
					fireTimeOutEvent();
				}
			}

		}
	}
	
	private synchronized void fireTimeOutEvent(){
		TimeOutEvent event = new TimeOutEvent(this, timedOut);
		if(listener!=null){
			listener.timeOutReceived(event);
		}
		else{
			this.stop();
		}
	}
	
	public Timer pause() {
		paused = true;
		return this;
	}

	public Timer resume(){
		paused = false;
		System.out.println("Resumed..");
		return this;
	}
	
	public int getElapsed()
	{
		return m_elapsed;
	}
	
	public boolean isTimedOut(){
		return timedOut;
	}
	
	public void stop()
	{
		stopped = true;
	}
	
	
	
}