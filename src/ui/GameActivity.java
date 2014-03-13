package ui;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import ui.GameActivity.GameActivityEvent.AmountEvent;
import ui.GameActivity.GameActivityEvent.CardsEvent;
import ui.GameActivity.GameActivityEvent.ClearCardsEvent;
import ui.GameActivity.GameActivityEvent.GameEndEvent;
import ui.GameActivity.GameActivityEvent.SitEvent;
import ui.GameActivity.GameActivityEvent.SittingPlayersChangedEvent;
import ui.GameActivity.GameActivityEvent.StandEvent;
import ui.GameActivity.GameActivityEvent.TableFullEvent;
import ui.GameActivity.GameActivityEvent.TokenEvent;
import ui.GameActivity.GameActivityEvent.TurnEndEvent;
import ui.GameActivity.GameActivityEvent.YourTurnEvent;
import ui.GameActivity.GameActivityEvent.TokenEvent.TokenType;
import utils.BitmapCache;

import logic.Card;
import logic.CommunicationBus;
import logic.Model;
import logic.PokerLogicController;
import logic.ClientModel.ClientModelEvent;
import logic.CommunicationBus.BusManager;
import logic.PokerLogicController.StartGameEvent;
import logic.ServerModel.GameState;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.ServiceState;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import chord.ClientGameChord;
import chord.GameChord;
import chord.GameChord.JoinedToServerEvent;
import chord.GameChord.ServerDisconnectedEvent;
import chord.ServerGameChord;

import com.example.android.wifidirect.R;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import static utils.Preconditions.checkState;

import events.BusEvent;

/*
 * Main game activity presented on the client side.
 */
public class GameActivity extends Activity implements BusManager {

	public static final String GAME_NAME = "POKER";
	public static final String CLIENT = "CLIENT";
	public static final String SERVER_NAME = "SERVER_NAME";

	private Bus mBus;
	private GameChord mGameChord;
	private PokerLogicController mLogicController;
	private List<BusManager> mManagers;
	private boolean mIsClient;
	private BitmapCache mMemoryCache;
	private Vibrator mVibrator;
	private Card mFirstCard;
	private Card mSecondCard;

	private View mBid5;
	private View mBid10;
	private View mBid20;
	private View mBid50;
	private View mBid100;
	private View mRaise;
	private View mAllIn;
	private View mFold;
	private View mStartGame;
	private TextView mAmount;
	private TextView mBidAmount;
	private TextView mMinimumBidAmount;
	private BitmapView mFirstCardView;
	private BitmapView mSecondCardView;
	private BitmapView mTokenView;
	private ToggleButton mSitStandButton;
	private ToggleButton mTurnCards;
	private ToggleButton mCallCheck;
	private List<View> mViews;

	private final BroadcastReceiver mWiFiBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final WifiInfo info = (WifiInfo) intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
			if (info == null) {
				finish();
				Toast.makeText(GameActivity.this, getString(R.string.wifi_disconnected), Toast.LENGTH_LONG).show();
			}
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_activity);

		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		registerWifiStateReceiver();

		final int memClass = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		final int cacheSize = 1024 * 1024 * memClass / 8;
		mMemoryCache = new BitmapCache(cacheSize, getAssets());

		final FrameLayout mGameActivityLayout = (FrameLayout) findViewById(R.id.game_activity_layout);

		final String userName = getSharedPreferences(MenuActivity.POKER_PREFERENCES, MODE_PRIVATE).getString(
				MenuActivity.USER_NAME_KEY, "");

		mBid5 = findViewById(R.id.button_bid_5);
		mBid10 = findViewById(R.id.button_bid_10);
		mBid20 = findViewById(R.id.button_bid_20);
		mBid50 = findViewById(R.id.button_bid_50);
		mBid100 = findViewById(R.id.button_bid_100);
		mCallCheck = (ToggleButton) findViewById(R.id.button_call_check);
		mRaise = findViewById(R.id.button_raise);
		mAllIn = findViewById(R.id.button_all_in);
		mFold = findViewById(R.id.button_fold);
		mBidAmount = (TextView) findViewById(R.id.bid_amount);
		mMinimumBidAmount = (TextView) findViewById(R.id.minimum_bid_amount);
		mAmount = (TextView) findViewById(R.id.amount);
		mFirstCardView = (BitmapView) findViewById(R.id.first_card);
		mSecondCardView = (BitmapView) findViewById(R.id.second_card);
		mTokenView = (BitmapView) findViewById(R.id.token);

		mTurnCards = (ToggleButton) findViewById(R.id.button_turn_cards);
		mTurnCards.setEnabled(false);
		mSitStandButton = (ToggleButton) findViewById(R.id.button_sit_stand);
		mSitStandButton.setEnabled(false);
		mStartGame = findViewById(R.id.start_game_button);
		mStartGame.setEnabled(false);

		TextView roomNameView = (TextView) findViewById(R.id.room_name);

		mViews = new LinkedList<View>();
		mViews.add(mBid5);
		mViews.add(mBid10);
		mViews.add(mBid20);
		mViews.add(mBid50);
		mViews.add(mBid100);
		mViews.add(mCallCheck);
		mViews.add(mRaise);
		mViews.add(mAllIn);
		mViews.add(mFold);

		for (View v : mViews) {
			v.setEnabled(false);
		}

		mBus = CommunicationBus.getInstance();
		mManagers = new LinkedList<BusManager>();
		mManagers.add(this);

		final Model model = new Model(!mIsClient);

		final Intent intent = getIntent();
		mIsClient = intent.getBooleanExtra(CLIENT, false);

		final String roomName;

		if (mIsClient) {
			roomName = getIntent().getStringExtra(SERVER_NAME);
			mGameChord = new ClientGameChord(this, roomName, GAME_NAME, userName);
			mStartGame.setVisibility(View.GONE);
		} else {
			//TODO:I am a server(table viewer?)
			roomName = getString(R.string.room).concat(UUID.randomUUID().toString().substring(0, 3));
			mGameChord = new ServerGameChord(this, roomName, GAME_NAME, userName);
//
//			ServiceConnector.createServiceProvider(this, new IServiceConnectEventListener() {
//
//				@Override
//				public void onCreated(ServiceProvider sprovider, ServiceState state) {
//					mServiceProvider = sprovider;
//					mManager = sprovider.getScreenCastManager();
//					if (mManager != null) {
//						mNoAllShareCastDialog.dismiss();
//						mAllShareDialog.show();
//						mManager.setScreenCastEventListener(new IScreenCastEventListener() {
//
//							@Override
//							public void onStopped(ScreenCastManager screencastmanager) {
//								mAllShareEnabled = false;
//								mAllShareDialog.show();
//							}
//
//							@Override
//							public void onStarted(ScreenCastManager screencastmanager) {
//								mAllShareEnabled = true;
//								mAllShareDialog.dismiss();
//								screencastmanager.setMode(ScreenMode.DUAL);
//							}
//						});
//
//						mGameboard = new PokerTableView(GameActivity.this, mManager, mMemoryCache, roomName);
//						mGameActivityLayout.addView(mGameboard, 0);
//						mGameboard.startBus();
//					}
//				}
//
//				@Override
//				public void onDeleted(ServiceProvider sprovider) {
//				}
//			});
//
//			mLogicController = new PokerLogicController(model, getResources());
//			mManagers.add(mLogicController);
		}

		roomNameView.setText(roomName);
		mManagers.add(model);
		mManagers.add(mGameChord);

		for (BusManager manager : mManagers) {
			manager.startBus();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!mIsClient) {
			//i am server
		}
	}

	@Override
	public void onBackPressed() {
		// @formatter:off
		new AlertDialog.Builder(this)
		.setMessage(R.string.exit)
		.setPositiveButton(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (mSitStandButton.isChecked()) {
					postClientModelEvent(new ClientModelEvent.StandEvent());
				}
				GameActivity.this.finish();
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.show();
		// @formatter:on
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {

		for (BusManager manager : mManagers) {
			manager.stopBus();
		}

//		if (mGameboard != null) {
//			mGameboard.stopBus();
//		}
//
//		mGameChord.stopChord();
//		if (mManager != null) {
//			mManager.stop();
//		}
//
//		if (mServiceProvider != null) {
//			ServiceConnector.deleteServiceProvider(mServiceProvider);
//		}

		unregisterReceiver(mWiFiBroadcastReceiver);

		super.onDestroy();
	}

	private void registerWifiStateReceiver() {
		final IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		registerReceiver(mWiFiBroadcastReceiver, filter);
	}

	public void startGame(View v) {
		mBus.post(StartGameEvent.INSTANCE);
		mStartGame.setEnabled(false);
	}

	public void bid(View v) {
		final int amount;

		switch (v.getId()) {
		case R.id.button_bid_5:
			amount = 5;
			break;
		case R.id.button_bid_10:
			amount = 10;
			break;
		case R.id.button_bid_20:
			amount = 20;
			break;
		case R.id.button_bid_50:
			amount = 50;
			break;
		case R.id.button_bid_100:
			amount = 100;
			break;
		default:
			throw new IllegalArgumentException(Integer.toString(v.getId()));
		}

		postClientModelEvent(new ClientModelEvent.BidEvent(amount));
	}

	public void fold(View v) {
		postClientModelEvent(new ClientModelEvent.FoldEvent());
	}

	public void sitOrStand(View v) {
		if (((ToggleButton) v).isChecked()) {
			mSitStandButton.setEnabled(false);
			postClientModelEvent(new ClientModelEvent.SitEvent());
		} else {
			new AlertDialog.Builder(this).setMessage(R.string.stand)
					.setPositiveButton(R.string.ok, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							postClientModelEvent(new ClientModelEvent.StandEvent());
						}
					}).setNegativeButton(R.string.cancel, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mSitStandButton.setChecked(true);
						}
					}).create().show();
		}
	}

	public void allIn(View v) {
		postClientModelEvent(new ClientModelEvent.AllInEvent());
	}

	public void rise(View v) {
		postClientModelEvent(new ClientModelEvent.RaiseEvent());
	}

	public void callOrCheck(View v) {
		if (((ToggleButton) v).isChecked()) {
			postClientModelEvent(new ClientModelEvent.CallEvent());
		} else {
			postClientModelEvent(new ClientModelEvent.CheckEvent());
		}
	}

	public void turnCards(View v) {
		if (mTurnCards.isChecked()) {
			mFirstCardView.setBitmap(mMemoryCache.getBitmap("client_card_back"));
			mSecondCardView.setBitmap(mMemoryCache.getBitmap("client_card_back"));
		} else {
			mFirstCardView.setBitmap(mMemoryCache.getBitmapForClientCard(mFirstCard));
			mSecondCardView.setBitmap(mMemoryCache.getBitmapForClientCard(mSecondCard));
		}
	}

	private <T extends ClientModelEvent> void postClientModelEvent(T event) {
		mBus.post(event);
	}

	/**
	 * Disables all the buttons except the stand and turn cards button.
	 * 
	 * @param event
	 *            the turn end event
	 */
	@Subscribe
	public void turnEnd(TurnEndEvent event) {
		for (View v : mViews) {
			v.setEnabled(false);
		}
	}

	/**
	 * Enables fold, all and check or call buttons. Also enables appropriate bid buttons.
	 * 
	 * @param event
	 *            the your turn event
	 */
	@Subscribe
	public void yourTurn(YourTurnEvent event) {
		switch (event.getTurnType()) {
		case CALL:
			mCallCheck.setEnabled(true);
			mCallCheck.setChecked(false);
			break;
		case CHECK:
			mCallCheck.setEnabled(true);
			mCallCheck.setChecked(true);
			break;
		case NONE:
			// Does nothing intentionally.
			break;
		default:
			throw new IllegalArgumentException(event.getTurnType().name());
		}

		mFold.setEnabled(true);
		mAllIn.setEnabled(true);

		mMinimumBidAmount.setText(Integer.toString(event.getMinimumBidAmount()));
		final int amount = event.getAmount();

		if (amount >= 100) {
			mBid100.setEnabled(true);
			mBid100.setVisibility(View.VISIBLE);
			mBid50.setEnabled(true);
			mBid50.setVisibility(View.VISIBLE);
			mBid20.setEnabled(true);
			mBid20.setVisibility(View.VISIBLE);
			mBid10.setEnabled(true);
			mBid10.setVisibility(View.VISIBLE);
			mBid5.setEnabled(true);
			mBid5.setVisibility(View.VISIBLE);
		} else if (amount < 10 && amount >= 5) {
			mBid5.setVisibility(View.VISIBLE);
			mBid5.setEnabled(true);
		} else if (amount < 20) {
			mBid10.setVisibility(View.VISIBLE);
			mBid10.setEnabled(true);
			mBid5.setVisibility(View.VISIBLE);
			mBid5.setEnabled(true);
		} else if (amount < 50) {
			mBid20.setVisibility(View.VISIBLE);
			mBid20.setEnabled(true);
			mBid10.setVisibility(View.VISIBLE);
			mBid10.setEnabled(true);
			mBid5.setVisibility(View.VISIBLE);
			mBid5.setEnabled(true);
		} else if (amount < 100) {
			mBid50.setVisibility(View.VISIBLE);
			mBid50.setEnabled(true);
			mBid20.setVisibility(View.VISIBLE);
			mBid20.setEnabled(true);
			mBid10.setVisibility(View.VISIBLE);
			mBid10.setEnabled(true);
			mBid5.setVisibility(View.VISIBLE);
			mBid5.setEnabled(true);
		}

		mVibrator.vibrate(400);
	}

	/**
	 * Disables all the buttons except the stand one. Sets bid and minimum bid amounts to zero.
	 * 
	 * @param event
	 *            the game end event
	 */
	@Subscribe
	public void gameEnd(GameEndEvent event) {
		mBidAmount.setText(Integer.toString(0));
		mMinimumBidAmount.setText(Integer.toString(0));
		mTokenView.setVisibility(View.INVISIBLE);

		for (View v : mViews) {
			v.setEnabled(false);
		}
	}

	/**
	 * Refreshes the amounts (player's amount, bid amount, minimum bid amount), disables the bid buttons if necessary,
	 * enables the rise button when the bid amount is grater than the minimum bid.
	 * 
	 * @param event
	 *            the amount event
	 */
	@Subscribe
	public void amount(AmountEvent event) {
		final int amount = event.getAmount();
		final int bidAmount = event.getBidAmount();
		mAmount.setText(Integer.toString(amount));
		mBidAmount.setText(Integer.toString(bidAmount));
		mMinimumBidAmount.setText(Integer.toString(event.getMinimumBidAmount()));

		if (amount < 5) {
			mBid100.setVisibility(View.INVISIBLE);
			mBid100.setEnabled(true);
			mBid50.setVisibility(View.INVISIBLE);
			mBid50.setEnabled(true);
			mBid20.setVisibility(View.INVISIBLE);
			mBid20.setEnabled(true);
			mBid10.setVisibility(View.INVISIBLE);
			mBid10.setEnabled(true);
			mBid5.setVisibility(View.INVISIBLE);
			mBid5.setEnabled(true);
		} else if (amount < 10) {
			mBid100.setVisibility(View.INVISIBLE);
			mBid100.setEnabled(true);
			mBid50.setVisibility(View.INVISIBLE);
			mBid50.setEnabled(true);
			mBid20.setVisibility(View.INVISIBLE);
			mBid20.setEnabled(true);
			mBid10.setVisibility(View.INVISIBLE);
			mBid10.setEnabled(true);
			mBid5.setVisibility(View.VISIBLE);
		} else if (amount < 20) {
			mBid100.setVisibility(View.INVISIBLE);
			mBid100.setEnabled(true);
			mBid50.setVisibility(View.INVISIBLE);
			mBid50.setEnabled(true);
			mBid20.setVisibility(View.INVISIBLE);
			mBid20.setEnabled(true);
			mBid10.setVisibility(View.VISIBLE);
			mBid5.setVisibility(View.VISIBLE);
		} else if (amount < 50) {
			mBid100.setVisibility(View.INVISIBLE);
			mBid100.setEnabled(true);
			mBid50.setVisibility(View.INVISIBLE);
			mBid50.setEnabled(true);
			mBid20.setVisibility(View.VISIBLE);
			mBid10.setVisibility(View.VISIBLE);
			mBid5.setVisibility(View.VISIBLE);
		} else if (amount < 100) {
			mBid100.setVisibility(View.INVISIBLE);
			mBid100.setEnabled(true);
			mBid50.setVisibility(View.VISIBLE);
			mBid20.setVisibility(View.VISIBLE);
			mBid10.setVisibility(View.VISIBLE);
			mBid5.setVisibility(View.VISIBLE);
		} else {
			mBid100.setVisibility(View.VISIBLE);
			mBid50.setVisibility(View.VISIBLE);
			mBid20.setVisibility(View.VISIBLE);
			mBid10.setVisibility(View.VISIBLE);
			mBid5.setVisibility(View.VISIBLE);
		}

		if (bidAmount > event.getMinimumBidAmount()) {
			mRaise.setEnabled(true);
			mCallCheck.setEnabled(false);
		}
	}

	@Subscribe
	public void sit(SitEvent event) {
		mSitStandButton.setEnabled(true);
	}

	/**
	 * Disables all the buttons except the sit button.
	 * 
	 * @param event
	 *            the stand event
	 */
	@Subscribe
	public void stand(StandEvent event) {
		for (View v : mViews) {
			v.setEnabled(false);
		}
	}

	/**
	 * Shows received token on the screen.
	 * 
	 * @param event
	 *            the token event
	 */
	@Subscribe
	public void token(TokenEvent event) {
		if (event.getTokenType() == TokenType.NONE) {
			mTokenView.setVisibility(View.INVISIBLE);
		} else {
			final Bitmap tokenBitmap = mMemoryCache.getBitmapForClientToken(event.getTokenType());
			mTokenView.setBitmap(tokenBitmap);
			mTokenView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Shows received cards on the screen.
	 * 
	 * @param event
	 *            the cards event
	 */
	@Subscribe
	public void cards(CardsEvent event) {
		mTurnCards.setEnabled(true);
		mTurnCards.setChecked(false);
		final Pair<Card, Card> cards = event.getCards();

		Bitmap firstCard = null;
		Bitmap secondCard = null;

		if (cards != null) {
			mTurnCards.setEnabled(true);
			mFirstCard = cards.first;
			mSecondCard = cards.second;
			firstCard = mMemoryCache.getBitmapForClientCard(cards.first);
			secondCard = mMemoryCache.getBitmapForClientCard(cards.second);
		} else {
			mTurnCards.setEnabled(false);
		}

		mFirstCardView.setBitmap(firstCard);
		mSecondCardView.setBitmap(secondCard);
	}

	/**
	 * Handles the full table situation.
	 * 
	 * @param event
	 *            the table full event
	 */
	@Subscribe
	public void tableFull(TableFullEvent event) {
		new AlertDialog.Builder(this).setTitle(getString(R.string.table_full_dialog_title))
				.setMessage(getString(R.string.table_full_dialog_msg))
				.setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mSitStandButton.setEnabled(true);
						mSitStandButton.setChecked(false);
					}
				}).create().show();
	}

	@Subscribe
	public void clearCards(ClearCardsEvent event) {
		mFirstCardView.setBitmap(null);
		mSecondCardView.setBitmap(null);
	}

	@Subscribe
	public void onSittingPlayersCountChanged(SittingPlayersChangedEvent event) {
		final int sittingPlayers = event.getSittingPlayersCount();
		checkState(sittingPlayers >= 0);

		if (sittingPlayers > 1 && !event.isGameOngoing()) {
			mStartGame.setEnabled(true);
		} else {
			mStartGame.setEnabled(false);
		}
	}

	@Override
	public void startBus() {
		mBus.register(this);
	}

	@Override
	public void stopBus() {
		mBus.unregister(this);
	}

	@Subscribe
	public void onServerDisconnected(ServerDisconnectedEvent event) {
		Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT).show();
		finish();
	}

	@Subscribe
	public void joinedToServer(JoinedToServerEvent event) {
		mSitStandButton.setEnabled(true);
	}

	/**
	 * Event posted through the {@link Bus} that contains information relevant for the {@link GameActivity}.
	 */
	public static class GameActivityEvent extends BusEvent {

		private static final long serialVersionUID = 20130326L;

		GameActivityEvent() {
			super();
		}

		public static class YourTurnEvent extends GameActivityEvent {

			private static final long serialVersionUID = 20130326L;

			private final YourTurnType mTurnType;
			private final int mMinimumBidAmount;
			private final int mAmount;

			public YourTurnEvent(YourTurnType turnType, int amount, int minimumBidAmount) {
				super();
				mTurnType = turnType;
				mMinimumBidAmount = minimumBidAmount;
				mAmount = amount;
			}

			public YourTurnType getTurnType() {
				return mTurnType;
			}

			public int getMinimumBidAmount() {
				return mMinimumBidAmount;
			}

			public int getAmount() {
				return mAmount;
			}

			public enum YourTurnType {
				//@formatter:off
				CHECK,
				CALL,
				NONE;
				//@formatter:on

				@Override
				public String toString() {
					return name();
				};

			}

		}

		public static class TurnEndEvent extends GameActivityEvent {

			private static final long serialVersionUID = 20130326L;

			public TurnEndEvent() {
				super();
			}

		}

		public static class AmountEvent extends GameActivityEvent {

			private static final long serialVersionUID = 20130326L;

			private final int mAmount;
			private final int mBidAmount;
			private final int mMinimumBidAmount;

			public AmountEvent(int amount, int bidAmount, int minimumBidAmount) {
				super();
				mAmount = amount;
				mBidAmount = bidAmount;
				mMinimumBidAmount = minimumBidAmount;
			}

			public int getAmount() {
				return mAmount;
			}

			public int getBidAmount() {
				return mBidAmount;
			}

			public int getMinimumBidAmount() {
				return mMinimumBidAmount;
			}

		}

		public static class TokenEvent extends GameActivityEvent {

			private static final long serialVersionUID = 20130326L;

			private final TokenType mTokenType;

			public TokenEvent(TokenType tokenType) {
				super();
				mTokenType = tokenType;
			}

			public TokenType getTokenType() {
				return mTokenType;
			}

			public enum TokenType {
				//@formatter:off
				SMALL_BLIND,
				BIG_BLIND,
				DEALER_WITH_SMALL_BLIND,
				DEALER,
				NONE;
				//@formatter:on

				@Override
				public String toString() {
					return name();
				}

			}

		}

		public static class CardsEvent extends GameActivityEvent {

			private static final long serialVersionUID = 20130326L;

			private final transient Pair<Card, Card> mCards;

			public CardsEvent(Pair<Card, Card> cards) {
				super();
				mCards = cards;
			}

			public Pair<Card, Card> getCards() {
				return mCards;
			}

		}

		public static class SitEvent extends GameActivityEvent {

			private static final long serialVersionUID = 20130326L;

			public SitEvent() {
				super();
			}

		}

		public static class StandEvent extends GameActivityEvent {

			private static final long serialVersionUID = 20130326L;

			public StandEvent() {
				super();
			}

		}

		public static class GameEndEvent extends GameActivityEvent {

			private static final long serialVersionUID = 20130326L;

			public GameEndEvent() {
				super();
			}

		}

		public static class TableFullEvent extends GameActivityEvent {

			private static final long serialVersionUID = 20130329L;

			public TableFullEvent() {
				super();
			}

		}

		public static class SittingPlayersChangedEvent extends GameActivityEvent {

			private static final long serialVersionUID = 20130410L;
			private final int mSittingPlayersCount;
			private final GameState mGameState;

			private static final EnumSet<GameState> ONGOING_GAME = EnumSet.of(GameState.FLOP, GameState.PRE_FLOP,
					GameState.RIVER, GameState.TURN);

			public SittingPlayersChangedEvent(GameState gameState, int sittingPlayersCount) {
				super();
				mSittingPlayersCount = sittingPlayersCount;
				mGameState = gameState;
			}

			public int getSittingPlayersCount() {
				return mSittingPlayersCount;
			}

			public boolean isGameOngoing() {
				return ONGOING_GAME.contains(mGameState);
			}
		}

		public static class ClearCardsEvent extends GameActivityEvent {

			private static final long serialVersionUID = 20140417L;

			public ClearCardsEvent() {
				super();
			}
		}

	}

}
