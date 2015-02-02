package fr.carmelo.game.activities;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import fr.carmelo.game.support_classes.PauseView;
import fr.carmelo.game.support_classes.PlaygroundView;

public abstract class AbstractLevelActivity extends Activity {

	private RelativeLayout layoutRoot;
	private PauseView pauseView;
	private PlaygroundView playView;

	private Button pauseButton;
	private Button resumeButton;

	protected List<Button> listButtons;

	// private volatile int counter;

	protected GameThread gThread;

	protected volatile boolean running;
	private volatile boolean threadNotStarted;

	protected boolean pauseCanShow;

	private TextView score;

	// private int[] randomArray;

	// private final static int nbOfShots = 20;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.level);

		layoutRoot = (RelativeLayout) findViewById(R.id.level_root);

		playView = new PlaygroundView(getApplicationContext());
		layoutRoot.addView(playView);

		listButtons = playView.getListButtons();

		pauseView = new PauseView(getApplicationContext());
		pauseView.setVisibility(View.GONE);
		layoutRoot.addView(pauseView);

		pauseButton = (Button) findViewById(R.id.btn_pause);
		PauseClickListener pauseClickLis = new PauseClickListener();
		pauseButton.setOnClickListener(pauseClickLis);

		resumeButton = (Button) findViewById(R.id.btn_resume);
		ResumeClickListener resClickLis = new ResumeClickListener();
		resumeButton.setOnClickListener(resClickLis);

		score = (TextView) findViewById(R.id.tv_score);
		threadNotStarted = true;

		// randomArray = RandomGenerator.randomArray(nbOfShots,
		// listButtons.size());

		// counter = 0;
		pauseCanShow = true;
		resume();
	}

	// @Override
	// protected void onResume() {
	// super.onResume();
	// playView.setVisibility(View.VISIBLE);
	//
	// running = true;
	//
	// gThread = new GameThread();
	// gThread.start();
	// }

	private synchronized void resume() {
		playView.setVisibility(View.VISIBLE);
		running = true;

		if (threadNotStarted) {
			gThread = new GameThread();
			gThread.start();
			threadNotStarted = false;
		} else {
			gThread.resumeThread();
		}
		
		
	}

	@Override
	public void onPause() {
		super.onPause();
		// pause();

		running = false;

		playView.setVisibility(View.GONE);

		if (pauseCanShow) {
			pauseView.setVisibility(View.VISIBLE);
		}

		while (true) {
			try {
				gThread.join();
				threadNotStarted = true;
				return;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private synchronized void pause() {
		running = false;
		gThread.suspendThread();

		playView.setVisibility(View.GONE);

		if (pauseCanShow) {
			pauseView.setVisibility(View.VISIBLE);
		}

		// while (true) {
		// try {
		// gThread.join();
		// return;
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		pauseCanShow = false;
	}

	protected void setScore(final String points) {
		score.post(new Runnable() {

			@Override
			public void run() {
				score.setText(points);
			}
		});
	}

	protected abstract void gameThreadRun();

	protected class GameThread extends Thread {

		protected boolean suspended;

		private synchronized void suspendThread() {
			suspended = true;
		}

		private synchronized void resumeThread() {
			suspended = false;
			notify();
		}

		protected synchronized void checkSuspension() {
			while (suspended) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void run() {
			Log.d("AbstractLevelActivity", " HHH");
			gameThreadRun();
		}
	}

	private class PauseClickListener implements OnClickListener {

		@Override
		public synchronized void onClick(View arg0) {
			pause();
		}
	}

	private class ResumeClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {

			resume();

			pauseView.setVisibility(View.GONE);
		}
	}

}
