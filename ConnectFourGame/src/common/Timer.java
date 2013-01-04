package common;


import java.util.EventObject;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * Class representing a Timer.
 */
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
	
	private Box timerBox;
	private JLabel timerText;
	
	/**
	 * Defines an interface for a timer listeners.
	 */
	public interface TimerListener 
	{
	    public void timeOutReceived(TimeOutEvent event );
	}
	
	/**
	 * Class representing the timeout event.
	 */
	public class TimeOutEvent extends EventObject {
	    private boolean value;
	    
	    /**
	     * Creates a new timeout event.
	     * @param source
	     * @param value
	     */
	    public TimeOutEvent( Object source, boolean value ) {
	        super( source );
	        this.value = value;
	    }
	    
	    /**
	     * Returns the event instance value.
	     * @return value
	     */
	    public boolean getValue() {
	        return value;
	    }
	    
	}
	
	/**
	 * Create a timer for specific timeout and timer listener.
	 * @param lengthInSeconds
	 * @param lst
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
		timerText = null;
		timerBox = null;
	}

	/**
	 * Gets the timer thread.
	 * @return timerThread
	 */
	public Thread getTimerThread(){
		return timerThread;
	}
	
	/**
	 * Starts the timer and return it.
	 * @return this
	 */
	public Timer start()
	{
		timerThread.start();
		return this;
	}

	/**
	 * Resets the timer and return it.
	 * @return this
	 */
	public Timer reset()
	{
		m_elapsed = 0;
		timedOut = false;
		updateTimerText();
		return this;
	}

	/**
	 * The timer main thread. Counts the seconds.
	 */
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
				this.updateTimerText();
				// Check to see if the time has been exceeded
				if (m_elapsed >= m_length)
				{
					m_elapsed -= m_rate;
					timedOut = true;
					fireTimeOutEvent();
				}
			}

		}
	}
	
	/**
	 * Updates the timer text with current time.
	 */
	private void updateTimerText() {
		if(timerText != null){
			int currT = m_length-m_elapsed;
			timerText.setText(" " + currT + " sec");
		}
	}

	/**
	 * Fires an timeout event.
	 */
	private synchronized void fireTimeOutEvent(){
		TimeOutEvent event = new TimeOutEvent(this, timedOut);
		if(listener!=null){
			listener.timeOutReceived(event);
		}
		else{
			this.stop();
		}
	}
	
	/**
	 * Pauses the timer.
	 * @return this
	 */
	public Timer pause() {
		paused = true;
		return this;
	}

	/**
	 * Resumes the timer.
	 * @return this
	 */
	public Timer resume(){
		paused = false;
		return this;
	}
	
	/**
	 * Gets the elapsed time.
	 * @return m_elapsed
	 */
	public int getElapsed()
	{
		return m_elapsed;
	}
	
	/**
	 * Returns true if the timeout event was already fired.
	 * @return timedOut
	 */
	public boolean isTimedOut(){
		return timedOut;
	}
	
	/**
	 * Restarts the timer and updates the timer text.
	 */
	public void restart(){
		this.pause().reset().resume();
		updateTimerText();
	}
	
	/**
	 * Updates the timer with a specific time.
	 * @param time
	 */
	public void updateTimer(int time){
		this.pause();
		m_elapsed = time;
		this.updateTimerText();
		this.resume();
	}
	
	/**
	 * Stops the timer.
	 */
	public void stop()
	{
		stopped = true;
	}
	
	/**
	 * Creates the timer box containing the timer text.
	 * @return
	 */
	public Box createTimerBox(){
		if(timerBox != null){
			return timerBox;
		}
		timerBox = Box.createHorizontalBox();
		timerText = new JLabel();
		timerText.setHorizontalAlignment(SwingUtilities.RIGHT);
		timerText.setText(" " + m_length + "sec");
		timerBox.add(timerText);
		timerText.setEnabled(true);
		return timerBox;
	}
}