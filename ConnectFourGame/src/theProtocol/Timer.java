package theProtocol;

public class Timer implements Runnable
{
	// Rate at which timer is checked in seconds 
	protected int m_rate = 1;
	
	// Length of timeout 
	private int m_length;

	// Time elapsed 
	private int m_elapsed;

	//The thread that should run the timer
	private Thread timerThread;
	/**
	  * Creates a timer of a specified length
	  * @param	length	Length of time before timeout occurs
	  */
	public Timer ( int lengthInSeconds )
	{
		// Assign to member variable
		m_length = lengthInSeconds;

		// Set time elapsed
		m_elapsed = 0;
		
		timerThread = new Thread(this);
	}

	
	public void start()
	{
		timerThread.start();
	}
	/** Resets the timer back to zero */
	public synchronized void reset()
	{
		m_elapsed = 0;
	}

	/** Performs timer specific code */
	public void run()
	{
		// Keep looping
		for (;;)
		{
			// Put the timer to sleep
			try
			{ 
				Thread.sleep(m_rate*1000);
			}
			catch (InterruptedException ioe) 
			{
				return;
			}

			// Use 'synchronized' to prevent conflicts
			synchronized ( this )
			{
				// Increment time remaining
				m_elapsed += m_rate;

				// Check to see if the time has been exceeded
				if (m_elapsed > m_length)
				{
					// Trigger a timeout
					timeout();
				}
			}

		}
	}

	public int getElapsed()
	{
		return m_elapsed;
	}
	private void timeout()
	{
		return;
	}
	
	public boolean isTimedOut(){
		return (m_length <= m_elapsed);
	}
	
	public void abort()
	{
		timerThread.interrupt();
	}
	
	@SuppressWarnings("deprecation")
	public void delete(){
		timerThread.stop();
	}
	
	
}