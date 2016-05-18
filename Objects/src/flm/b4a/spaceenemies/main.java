package flm.b4a.spaceenemies;

import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new BA(this.getApplicationContext(), null, null, "flm.b4a.spaceenemies", "flm.b4a.spaceenemies.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		mostCurrent = this;
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
		BA.handler.postDelayed(new WaitForLayout(), 5);

	}
	private static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "flm.b4a.spaceenemies", "flm.b4a.spaceenemies.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "flm.b4a.spaceenemies.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
		return true;
	}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
		this.setIntent(intent);
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null) //workaround for emulator bug (Issue 2423)
            return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}

public anywheresoftware.b4a.keywords.Common __c = null;
public flm.b4a.accelerview.AcceleratedSurface _acsf = null;
public flm.b4a.accelerview.AcceleratedSurface _acsftext = null;
public flm.b4a.accelerview.ImageUtils _iu = null;
public static boolean _texttoshow = false;
public static String _text = "";
public static int _maxtextsize = 0;
public flm.b4a.accelerview.ComplexText _textobj = null;
public static int _score = 0;
public static int _scorefontsize = 0;
public static int _scoreheight = 0;
public flm.b4a.spaceenemies.main._typhero _hero = null;
public anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _bmptux = null;
public anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _bmpminitux = null;
public anywheresoftware.b4a.objects.drawable.CanvasWrapper.PathWrapper _shippath = null;
public static byte _lives = (byte)0;
public anywheresoftware.b4a.objects.collections.List _enemies = null;
public flm.b4a.spaceenemies.main._typwave _wave = null;
public anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _bmpdroid = null;
public anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _bmpdroid2 = null;
public static int _waitbeforedropping = 0;
public static int _waitbeforenextbomb = 0;
public anywheresoftware.b4a.objects.collections.List _bombs = null;
public static float _bombspeed = 0f;
public static float _bombrotationspeed = 0f;
public anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _bmpbomb = null;
public anywheresoftware.b4a.objects.collections.List _lasershots = null;
public static float _laserspeed = 0f;
public static int _lasershotsize = 0;
public static int _waitbeforenextshot = 0;
public anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _bmpaleft = null;
public anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper _bmparight = null;
public static float _bitmapsize = 0f;
public static long _elapsed = 0L;
public anywheresoftware.b4a.audio.SoundPoolWrapper _sp = null;
public static int _lasersound = 0;
public static int _hitsound = 0;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static class _typhero{
public boolean IsInitialized;
public float X;
public float Y;
public int ShipLeft;
public int ShipRight;
public int ShipCenter;
public int ShipTop;
public long FireWait;
public short Move;
public short DyingState;
public void Initialize() {
IsInitialized = true;
X = 0f;
Y = 0f;
ShipLeft = 0;
ShipRight = 0;
ShipCenter = 0;
ShipTop = 0;
FireWait = 0L;
Move = (short)0;
DyingState = (short)0;
}
@Override
		public String toString() {
			return BA.TypeToString(this, false);
		}}
public static class _typenemy{
public boolean IsInitialized;
public int OffsetX;
public int OffsetY;
public byte State;
public long FireWait;
public void Initialize() {
IsInitialized = true;
OffsetX = 0;
OffsetY = 0;
State = (byte)0;
FireWait = 0L;
}
@Override
		public String toString() {
			return BA.TypeToString(this, false);
		}}
public static class _typwave{
public boolean IsInitialized;
public int Count;
public float X;
public float Y;
public float Speed;
public long FireWait;
public void Initialize() {
IsInitialized = true;
Count = 0;
X = 0f;
Y = 0f;
Speed = 0f;
FireWait = 0L;
}
@Override
		public String toString() {
			return BA.TypeToString(this, false);
		}}
public static class _typbomb{
public boolean IsInitialized;
public float X;
public float Y;
public float Angle;
public boolean InverseRotation;
public void Initialize() {
IsInitialized = true;
X = 0f;
Y = 0f;
Angle = 0f;
InverseRotation = false;
}
@Override
		public String toString() {
			return BA.TypeToString(this, false);
		}}
public static class _typlaser{
public boolean IsInitialized;
public float X;
public float Y;
public void Initialize() {
IsInitialized = true;
X = 0f;
Y = 0f;
}
@Override
		public String toString() {
			return BA.TypeToString(this, false);
		}}
public static String  _acsf_draw(flm.b4a.accelerview.AcceleratedCanvas _ac) throws Exception{
int _i = 0;
flm.b4a.spaceenemies.main._typenemy _enemy = null;
flm.b4a.spaceenemies.main._typbomb _bomb = null;
flm.b4a.spaceenemies.main._typlaser _lasershot = null;
anywheresoftware.b4a.objects.drawable.CanvasWrapper.RectWrapper _r = null;
 //BA.debugLineNum = 355;BA.debugLine="Sub AcSf_Draw(AC As AS_Canvas)";
 //BA.debugLineNum = 357;BA.debugLine="AC.DrawText(Score, 0, ScoreHeight, Typeface.DEFAULT, ScoreFontSize, Colors.White, AC.ALIGN_LEFT)";
_ac.DrawText(BA.NumberToString(_score),(float) (0),(float) (_scoreheight),(anywheresoftware.b4a.keywords.constants.TypefaceWrapper) anywheresoftware.b4a.AbsObjectWrapper.ConvertToWrapper(new anywheresoftware.b4a.keywords.constants.TypefaceWrapper(), (android.graphics.Typeface)(anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT)),(float) (_scorefontsize),anywheresoftware.b4a.keywords.Common.Colors.White,_ac.ALIGN_LEFT);
 //BA.debugLineNum = 360;BA.debugLine="For i = 1 To Lives";
{
final int step269 = 1;
final int limit269 = (int) (_lives);
for (_i = (int) (1); (step269 > 0 && _i <= limit269) || (step269 < 0 && _i >= limit269); _i = ((int)(0 + _i + step269))) {
 //BA.debugLineNum = 361;BA.debugLine="AC.DrawBitmapAt(bmpMiniTux, 100%x - (i * bmpMiniTux.Width * 1.05), 0)";
_ac.DrawBitmapAt(mostCurrent._bmpminitux,(int) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA)-(_i*mostCurrent._bmpminitux.getWidth()*1.05)),(int) (0));
 }
};
 //BA.debugLineNum = 365;BA.debugLine="ShipPath.Initialize(Hero.ShipCenter, Hero.ShipTop)";
mostCurrent._shippath.Initialize((float) (mostCurrent._hero.ShipCenter),(float) (mostCurrent._hero.ShipTop));
 //BA.debugLineNum = 366;BA.debugLine="ShipPath.LineTo(Hero.ShipLeft, Hero.Y + bmpTux.Height)";
mostCurrent._shippath.LineTo((float) (mostCurrent._hero.ShipLeft),(float) (mostCurrent._hero.Y+mostCurrent._bmptux.getHeight()));
 //BA.debugLineNum = 367;BA.debugLine="ShipPath.LineTo(Hero.ShipRight, Hero.Y + bmpTux.Height)";
mostCurrent._shippath.LineTo((float) (mostCurrent._hero.ShipRight),(float) (mostCurrent._hero.Y+mostCurrent._bmptux.getHeight()));
 //BA.debugLineNum = 368;BA.debugLine="If Hero.DyingState = 0 Then";
if (mostCurrent._hero.DyingState==0) { 
 //BA.debugLineNum = 369;BA.debugLine="AC.DrawPath(ShipPath, Colors.Yellow, True, 0, True)";
_ac.DrawPath((Object)(mostCurrent._shippath.getObject()),anywheresoftware.b4a.keywords.Common.Colors.Yellow,anywheresoftware.b4a.keywords.Common.True,(float) (0),anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 370;BA.debugLine="AC.DrawBitmapAt(bmpTux, Hero.X, Hero.Y)";
_ac.DrawBitmapAt(mostCurrent._bmptux,(int) (mostCurrent._hero.X),(int) (mostCurrent._hero.Y));
 }else {
 //BA.debugLineNum = 373;BA.debugLine="AC.DrawPath(ShipPath, Colors.Red, True, 0, True)";
_ac.DrawPath((Object)(mostCurrent._shippath.getObject()),anywheresoftware.b4a.keywords.Common.Colors.Red,anywheresoftware.b4a.keywords.Common.True,(float) (0),anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 374;BA.debugLine="AC.DrawBitmapAt(IU.AlterColors(bmpTux, 128, 0, Hero.DyingState), Hero.X, Hero.Y)";
_ac.DrawBitmapAt((anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper) anywheresoftware.b4a.AbsObjectWrapper.ConvertToWrapper(new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper(), (android.graphics.Bitmap)(mostCurrent._iu.AlterColors(mostCurrent._bmptux,(int) (128),(int) (0),(float) (mostCurrent._hero.DyingState)))),(int) (mostCurrent._hero.X),(int) (mostCurrent._hero.Y));
 //BA.debugLineNum = 375;BA.debugLine="If Lives > 0 Then";
if (_lives>0) { 
 //BA.debugLineNum = 376;BA.debugLine="Hero.DyingState = Hero.DyingState + 1";
mostCurrent._hero.DyingState = (short) (mostCurrent._hero.DyingState+1);
 //BA.debugLineNum = 377;BA.debugLine="If Hero.DyingState > 12 Then Hero.DyingState = 0";
if (mostCurrent._hero.DyingState>12) { 
mostCurrent._hero.DyingState = (short) (0);};
 };
 };
 //BA.debugLineNum = 382;BA.debugLine="For i = Enemies.Size - 1 To 0 Step -1";
{
final int step286 = (int) (-1);
final int limit286 = (int) (0);
for (_i = (int) (mostCurrent._enemies.getSize()-1); (step286 > 0 && _i <= limit286) || (step286 < 0 && _i >= limit286); _i = ((int)(0 + _i + step286))) {
 //BA.debugLineNum = 383;BA.debugLine="Dim Enemy As typEnemy";
_enemy = new flm.b4a.spaceenemies.main._typenemy();
 //BA.debugLineNum = 384;BA.debugLine="Enemy = Enemies.Get(i)";
_enemy = (flm.b4a.spaceenemies.main._typenemy)(mostCurrent._enemies.Get(_i));
 //BA.debugLineNum = 385;BA.debugLine="If Enemy.State = 0 Then";
if (_enemy.State==0) { 
 //BA.debugLineNum = 386;BA.debugLine="AC.DrawBitmapAt(bmpDroid, Wave.X + Enemy.OffsetX, Wave.Y + Enemy.OffsetY)";
_ac.DrawBitmapAt(mostCurrent._bmpdroid,(int) (mostCurrent._wave.X+_enemy.OffsetX),(int) (mostCurrent._wave.Y+_enemy.OffsetY));
 }else if(_enemy.State==1) { 
 //BA.debugLineNum = 388;BA.debugLine="AC.DrawBitmapAt(bmpDroid2, Wave.X + Enemy.OffsetX, Wave.Y + Enemy.OffsetY)";
_ac.DrawBitmapAt(mostCurrent._bmpdroid2,(int) (mostCurrent._wave.X+_enemy.OffsetX),(int) (mostCurrent._wave.Y+_enemy.OffsetY));
 }else {
 //BA.debugLineNum = 391;BA.debugLine="AC.DrawBitmapAt(IU.AlterColors(bmpDroid, 255 - (Enemy.State * 10), 100, 2), _ 			                Wave.X + Enemy.OffsetX, Wave.Y + Enemy.OffsetY)";
_ac.DrawBitmapAt((anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper) anywheresoftware.b4a.AbsObjectWrapper.ConvertToWrapper(new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper(), (android.graphics.Bitmap)(mostCurrent._iu.AlterColors(mostCurrent._bmpdroid,(int) (255-(_enemy.State*10)),(int) (100),(float) (2)))),(int) (mostCurrent._wave.X+_enemy.OffsetX),(int) (mostCurrent._wave.Y+_enemy.OffsetY));
 //BA.debugLineNum = 393;BA.debugLine="Enemy.State = Enemy.State + 1";
_enemy.State = (byte) (_enemy.State+1);
 //BA.debugLineNum = 394;BA.debugLine="If Enemy.State > 20 Then";
if (_enemy.State>20) { 
 //BA.debugLineNum = 395;BA.debugLine="Enemies.RemoveAt(i)";
mostCurrent._enemies.RemoveAt(_i);
 //BA.debugLineNum = 396;BA.debugLine="If Enemies.Size = 0 Then";
if (mostCurrent._enemies.getSize()==0) { 
 //BA.debugLineNum = 398;BA.debugLine="Wave.Count = Wave.Count + 1";
mostCurrent._wave.Count = (int) (mostCurrent._wave.Count+1);
 //BA.debugLineNum = 399;BA.debugLine="If Wave.Count Mod 3 = 0 Then Lives = Lives + 1 'One extra life every 3 levels";
if (mostCurrent._wave.Count%3==0) { 
_lives = (byte) (_lives+1);};
 //BA.debugLineNum = 400;BA.debugLine="NextWave";
_nextwave();
 //BA.debugLineNum = 401;BA.debugLine="Bombs.Initialize";
mostCurrent._bombs.Initialize();
 //BA.debugLineNum = 402;BA.debugLine="LaserShots.Initialize";
mostCurrent._lasershots.Initialize();
 //BA.debugLineNum = 403;BA.debugLine="AnimateText(\"NEXT WAVE\")";
_animatetext("NEXT WAVE");
 };
 };
 };
 }
};
 //BA.debugLineNum = 410;BA.debugLine="Dim Bomb As typBomb";
_bomb = new flm.b4a.spaceenemies.main._typbomb();
 //BA.debugLineNum = 411;BA.debugLine="For i = 0 To Bombs.Size - 1";
{
final int step310 = 1;
final int limit310 = (int) (mostCurrent._bombs.getSize()-1);
for (_i = (int) (0); (step310 > 0 && _i <= limit310) || (step310 < 0 && _i >= limit310); _i = ((int)(0 + _i + step310))) {
 //BA.debugLineNum = 412;BA.debugLine="Bomb = Bombs.Get(i)";
_bomb = (flm.b4a.spaceenemies.main._typbomb)(mostCurrent._bombs.Get(_i));
 //BA.debugLineNum = 413;BA.debugLine="AC.MatrixSetRotate2(Bomb.Angle, Bit.ShiftRight(bmpBomb.Width, 1), Bit.ShiftRight(bmpBomb.Height, 1))";
_ac.MatrixSetRotate2(_bomb.Angle,(float) (anywheresoftware.b4a.keywords.Common.Bit.ShiftRight(mostCurrent._bmpbomb.getWidth(),(int) (1))),(float) (anywheresoftware.b4a.keywords.Common.Bit.ShiftRight(mostCurrent._bmpbomb.getHeight(),(int) (1))));
 //BA.debugLineNum = 414;BA.debugLine="AC.DrawBitmapWithMatrixAt(bmpBomb, Bomb.X, Bomb.Y, True)";
_ac.DrawBitmapWithMatrixAt(mostCurrent._bmpbomb,(int) (_bomb.X),(int) (_bomb.Y),anywheresoftware.b4a.keywords.Common.True);
 }
};
 //BA.debugLineNum = 418;BA.debugLine="Dim LaserShot As typLaser, R As Rect";
_lasershot = new flm.b4a.spaceenemies.main._typlaser();
_r = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.RectWrapper();
 //BA.debugLineNum = 419;BA.debugLine="For i = 0 To LaserShots.Size - 1";
{
final int step316 = 1;
final int limit316 = (int) (mostCurrent._lasershots.getSize()-1);
for (_i = (int) (0); (step316 > 0 && _i <= limit316) || (step316 < 0 && _i >= limit316); _i = ((int)(0 + _i + step316))) {
 //BA.debugLineNum = 420;BA.debugLine="LaserShot = LaserShots.Get(i)";
_lasershot = (flm.b4a.spaceenemies.main._typlaser)(mostCurrent._lasershots.Get(_i));
 //BA.debugLineNum = 421;BA.debugLine="R.Initialize(LaserShot.X, LaserShot.Y, LaserShot.X + 0.5%x, LaserShot.Y + LaserShotSize)";
_r.Initialize((int) (_lasershot.X),(int) (_lasershot.Y),(int) (_lasershot.X+anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0.5),mostCurrent.activityBA)),(int) (_lasershot.Y+_lasershotsize));
 //BA.debugLineNum = 422;BA.debugLine="AC.DrawRect(R, Colors.Cyan, True, 0, False)";
_ac.DrawRect(_r,anywheresoftware.b4a.keywords.Common.Colors.Cyan,anywheresoftware.b4a.keywords.Common.True,(float) (0),anywheresoftware.b4a.keywords.Common.False);
 }
};
 //BA.debugLineNum = 426;BA.debugLine="AC.DrawBitmapAt(bmpALeft, 0, 100%y - bmpALeft.Height)";
_ac.DrawBitmapAt(mostCurrent._bmpaleft,(int) (0),(int) (anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA)-mostCurrent._bmpaleft.getHeight()));
 //BA.debugLineNum = 427;BA.debugLine="AC.DrawBitmapAt(bmpARight, bmpALeft.Width, 100%y - bmpARight.Height)";
_ac.DrawBitmapAt(mostCurrent._bmparight,mostCurrent._bmpaleft.getWidth(),(int) (anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA)-mostCurrent._bmparight.getHeight()));
 //BA.debugLineNum = 428;BA.debugLine="End Sub";
return "";
}
public static String  _acsf_touch(int _action,int _x,int _y,Object _event) throws Exception{
flm.b4a.spaceenemies.main._typlaser _lasershot = null;
 //BA.debugLineNum = 430;BA.debugLine="Sub AcSf_Touch(Action As Int, X As Int, Y As Int, Event As Object)";
 //BA.debugLineNum = 431;BA.debugLine="If Lives > 0 AND Not(TextToShow) Then";
if (_lives>0 && anywheresoftware.b4a.keywords.Common.Not(_texttoshow)) { 
 //BA.debugLineNum = 433;BA.debugLine="If Action = 0 OR Action = 2 Then";
if (_action==0 || _action==2) { 
 //BA.debugLineNum = 434;BA.debugLine="If X <= bmpALeft.Width Then";
if (_x<=mostCurrent._bmpaleft.getWidth()) { 
 //BA.debugLineNum = 435;BA.debugLine="Hero.Move = -1";
mostCurrent._hero.Move = (short) (-1);
 }else if(_x<=mostCurrent._bmpaleft.getWidth()+mostCurrent._bmparight.getWidth()) { 
 //BA.debugLineNum = 437;BA.debugLine="Hero.Move = 1";
mostCurrent._hero.Move = (short) (1);
 }else {
 //BA.debugLineNum = 440;BA.debugLine="If Hero.FireWait <= 0 Then";
if (mostCurrent._hero.FireWait<=0) { 
 //BA.debugLineNum = 441;BA.debugLine="Dim LaserShot As typLaser";
_lasershot = new flm.b4a.spaceenemies.main._typlaser();
 //BA.debugLineNum = 442;BA.debugLine="LaserShot.X = Hero.X + Bit.ShiftRight(bmpTux.Width, 1) - 0.25%x";
_lasershot.X = (float) (mostCurrent._hero.X+anywheresoftware.b4a.keywords.Common.Bit.ShiftRight(mostCurrent._bmptux.getWidth(),(int) (1))-anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0.25),mostCurrent.activityBA));
 //BA.debugLineNum = 443;BA.debugLine="LaserShot.Y = Hero.Y - 1.5%y - LaserShotSize";
_lasershot.Y = (float) (mostCurrent._hero.Y-anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (1.5),mostCurrent.activityBA)-_lasershotsize);
 //BA.debugLineNum = 444;BA.debugLine="LaserShots.Add(LaserShot)";
mostCurrent._lasershots.Add((Object)(_lasershot));
 //BA.debugLineNum = 445;BA.debugLine="Hero.FireWait = WaitBeforeNextShot";
mostCurrent._hero.FireWait = (long) (_waitbeforenextshot);
 //BA.debugLineNum = 446;BA.debugLine="SP.Play(LaserSound, 1 - (LaserShot.X / 100%x), LaserShot.X / 100%x, 1, 0, 1)";
mostCurrent._sp.Play(_lasersound,(float) (1-(_lasershot.X/(double)anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA))),(float) (_lasershot.X/(double)anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA)),(int) (1),(int) (0),(float) (1));
 };
 };
 }else if(_action==1) { 
 //BA.debugLineNum = 450;BA.debugLine="Hero.Move = 0";
mostCurrent._hero.Move = (short) (0);
 };
 };
 //BA.debugLineNum = 453;BA.debugLine="End Sub";
return "";
}
public static String  _acsf_update(long _elapsedtime) throws Exception{
double _secs = 0;
boolean _edgereached = false;
flm.b4a.spaceenemies.main._typenemy _enemy = null;
int _i = 0;
int _bottom = 0;
flm.b4a.spaceenemies.main._typbomb _bomb = null;
flm.b4a.spaceenemies.main._typlaser _lasershot = null;
 //BA.debugLineNum = 235;BA.debugLine="Sub AcSf_Update(ElapsedTime As Long)";
 //BA.debugLineNum = 237;BA.debugLine="If TextToShow Then Return";
if (_texttoshow) { 
if (true) return "";};
 //BA.debugLineNum = 241;BA.debugLine="Elapsed = Min(ElapsedTime, 3 * 16)";
_elapsed = (long) (anywheresoftware.b4a.keywords.Common.Min(_elapsedtime,3*16));
 //BA.debugLineNum = 244;BA.debugLine="If Hero.Move <> 0 AND Hero.DyingState = 0 Then";
if (mostCurrent._hero.Move!=0 && mostCurrent._hero.DyingState==0) { 
 //BA.debugLineNum = 245;BA.debugLine="Hero.X = Min(Max(bmpALeft.Width + bmpARight.Width + 1%x, _ 		                 Hero.X + (Hero.Move * 0.5%x)), 99%x - bmpTux.Width)";
mostCurrent._hero.X = (float) (anywheresoftware.b4a.keywords.Common.Min(anywheresoftware.b4a.keywords.Common.Max(mostCurrent._bmpaleft.getWidth()+mostCurrent._bmparight.getWidth()+anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (1),mostCurrent.activityBA),mostCurrent._hero.X+(mostCurrent._hero.Move*anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0.5),mostCurrent.activityBA))),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (99),mostCurrent.activityBA)-mostCurrent._bmptux.getWidth()));
 //BA.debugLineNum = 247;BA.debugLine="Hero.ShipLeft = Hero.X - 1%x";
mostCurrent._hero.ShipLeft = (int) (mostCurrent._hero.X-anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (1),mostCurrent.activityBA));
 //BA.debugLineNum = 248;BA.debugLine="Hero.ShipRight = Hero.X + bmpTux.Width + 1%x";
mostCurrent._hero.ShipRight = (int) (mostCurrent._hero.X+mostCurrent._bmptux.getWidth()+anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (1),mostCurrent.activityBA));
 //BA.debugLineNum = 249;BA.debugLine="Hero.ShipCenter = Bit.ShiftRight(Hero.ShipLeft + Hero.ShipRight, 1)";
mostCurrent._hero.ShipCenter = anywheresoftware.b4a.keywords.Common.Bit.ShiftRight((int) (mostCurrent._hero.ShipLeft+mostCurrent._hero.ShipRight),(int) (1));
 };
 //BA.debugLineNum = 253;BA.debugLine="Dim Secs As Double = Elapsed / 1000";
_secs = _elapsed/(double)1000;
 //BA.debugLineNum = 254;BA.debugLine="Wave.X = Wave.X + (Wave.Speed * Secs)";
mostCurrent._wave.X = (float) (mostCurrent._wave.X+(mostCurrent._wave.Speed*_secs));
 //BA.debugLineNum = 255;BA.debugLine="Dim EdgeReached As Boolean = False";
_edgereached = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 256;BA.debugLine="If Wave.Speed > 0 Then";
if (mostCurrent._wave.Speed>0) { 
 //BA.debugLineNum = 257;BA.debugLine="EdgeReached = (Wave.X >= 95%x - (9 * BitmapSize * 1.2) - BitmapSize)";
_edgereached = (mostCurrent._wave.X>=anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (95),mostCurrent.activityBA)-(9*_bitmapsize*1.2)-_bitmapsize);
 }else {
 //BA.debugLineNum = 259;BA.debugLine="EdgeReached = (Wave.X <= 5%x)";
_edgereached = (mostCurrent._wave.X<=anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (5),mostCurrent.activityBA));
 };
 //BA.debugLineNum = 261;BA.debugLine="If EdgeReached Then";
if (_edgereached) { 
 //BA.debugLineNum = 263;BA.debugLine="Wave.Speed = -Wave.Speed";
mostCurrent._wave.Speed = (float) (-mostCurrent._wave.Speed);
 //BA.debugLineNum = 264;BA.debugLine="Wave.Y = Wave.Y + 2%y";
mostCurrent._wave.Y = (float) (mostCurrent._wave.Y+anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (2),mostCurrent.activityBA));
 };
 //BA.debugLineNum = 268;BA.debugLine="Wave.FireWait = Wave.FireWait - Elapsed";
mostCurrent._wave.FireWait = (long) (mostCurrent._wave.FireWait-_elapsed);
 //BA.debugLineNum = 269;BA.debugLine="If Wave.FireWait <= 0 Then";
if (mostCurrent._wave.FireWait<=0) { 
 //BA.debugLineNum = 271;BA.debugLine="Dim Enemy As typEnemy";
_enemy = new flm.b4a.spaceenemies.main._typenemy();
 //BA.debugLineNum = 272;BA.debugLine="Enemy = Enemies.Get(Rnd(0, Enemies.Size))";
_enemy = (flm.b4a.spaceenemies.main._typenemy)(mostCurrent._enemies.Get(anywheresoftware.b4a.keywords.Common.Rnd((int) (0),mostCurrent._enemies.getSize())));
 //BA.debugLineNum = 273;BA.debugLine="If Enemy.State <> 0 Then";
if (_enemy.State!=0) { 
 //BA.debugLineNum = 274;BA.debugLine="Enemy = Enemies.Get(Rnd(0, Enemies.Size))";
_enemy = (flm.b4a.spaceenemies.main._typenemy)(mostCurrent._enemies.Get(anywheresoftware.b4a.keywords.Common.Rnd((int) (0),mostCurrent._enemies.getSize())));
 };
 //BA.debugLineNum = 276;BA.debugLine="If Enemy.State = 0 Then";
if (_enemy.State==0) { 
 //BA.debugLineNum = 277;BA.debugLine="Enemy.State = 1 'Fire!";
_enemy.State = (byte) (1);
 //BA.debugLineNum = 278;BA.debugLine="Wave.FireWait = WaitBeforeNextBomb";
mostCurrent._wave.FireWait = (long) (_waitbeforenextbomb);
 };
 };
 //BA.debugLineNum = 281;BA.debugLine="For i = 0 To Enemies.Size - 1";
{
final int step204 = 1;
final int limit204 = (int) (mostCurrent._enemies.getSize()-1);
for (_i = (int) (0); (step204 > 0 && _i <= limit204) || (step204 < 0 && _i >= limit204); _i = ((int)(0 + _i + step204))) {
 //BA.debugLineNum = 282;BA.debugLine="Dim Enemy As typEnemy";
_enemy = new flm.b4a.spaceenemies.main._typenemy();
 //BA.debugLineNum = 283;BA.debugLine="Enemy = Enemies.Get(i)";
_enemy = (flm.b4a.spaceenemies.main._typenemy)(mostCurrent._enemies.Get(_i));
 //BA.debugLineNum = 284;BA.debugLine="Dim Bottom As Int = Wave.Y + Enemy.OffsetY + bmpDroid.Height";
_bottom = (int) (mostCurrent._wave.Y+_enemy.OffsetY+mostCurrent._bmpdroid.getHeight());
 //BA.debugLineNum = 285;BA.debugLine="If CollisionWithHero(Wave.X + Enemy.OffsetX, Wave.X + Enemy.OffsetX + bmpDroid.Width, Bottom) _ 		   OR Bottom >= 100%y Then";
if (_collisionwithhero((int) (mostCurrent._wave.X+_enemy.OffsetX),(int) (mostCurrent._wave.X+_enemy.OffsetX+mostCurrent._bmpdroid.getWidth()),_bottom) || _bottom>=anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA)) { 
 //BA.debugLineNum = 288;BA.debugLine="Hero.DyingState = 1";
mostCurrent._hero.DyingState = (short) (1);
 //BA.debugLineNum = 289;BA.debugLine="Lives = 0";
_lives = (byte) (0);
 //BA.debugLineNum = 290;BA.debugLine="AnimateText(\"GAME OVER\")";
_animatetext("GAME OVER");
 };
 //BA.debugLineNum = 292;BA.debugLine="If Enemy.State = 1 Then";
if (_enemy.State==1) { 
 //BA.debugLineNum = 294;BA.debugLine="Enemy.FireWait = Enemy.FireWait - Elapsed";
_enemy.FireWait = (long) (_enemy.FireWait-_elapsed);
 //BA.debugLineNum = 295;BA.debugLine="If Enemy.FireWait <= 0 Then";
if (_enemy.FireWait<=0) { 
 //BA.debugLineNum = 297;BA.debugLine="Enemy.State = 0 'Idle";
_enemy.State = (byte) (0);
 //BA.debugLineNum = 298;BA.debugLine="Enemy.FireWait = WaitBeforeDropping";
_enemy.FireWait = (long) (_waitbeforedropping);
 //BA.debugLineNum = 299;BA.debugLine="Dim Bomb As typBomb";
_bomb = new flm.b4a.spaceenemies.main._typbomb();
 //BA.debugLineNum = 300;BA.debugLine="Bomb.X = Wave.X + Enemy.OffsetX + Bit.ShiftRight(bmpDroid.Width - bmpBomb.Width, 1)";
_bomb.X = (float) (mostCurrent._wave.X+_enemy.OffsetX+anywheresoftware.b4a.keywords.Common.Bit.ShiftRight((int) (mostCurrent._bmpdroid.getWidth()-mostCurrent._bmpbomb.getWidth()),(int) (1)));
 //BA.debugLineNum = 301;BA.debugLine="Bomb.Y = Wave.Y + Enemy.OffsetY + Bit.ShiftRight(bmpDroid.Height, 1)";
_bomb.Y = (float) (mostCurrent._wave.Y+_enemy.OffsetY+anywheresoftware.b4a.keywords.Common.Bit.ShiftRight(mostCurrent._bmpdroid.getHeight(),(int) (1)));
 //BA.debugLineNum = 302;BA.debugLine="Bomb.InverseRotation = (Wave.Speed > 0)";
_bomb.InverseRotation = (mostCurrent._wave.Speed>0);
 //BA.debugLineNum = 303;BA.debugLine="If Bomb.InverseRotation Then";
if (_bomb.InverseRotation) { 
 //BA.debugLineNum = 304;BA.debugLine="Bomb.Angle = Rnd(0, 45)";
_bomb.Angle = (float) (anywheresoftware.b4a.keywords.Common.Rnd((int) (0),(int) (45)));
 }else {
 //BA.debugLineNum = 306;BA.debugLine="Bomb.Angle = Rnd(-45, 0)";
_bomb.Angle = (float) (anywheresoftware.b4a.keywords.Common.Rnd((int) (-45),(int) (0)));
 };
 //BA.debugLineNum = 308;BA.debugLine="Bombs.Add(Bomb)";
mostCurrent._bombs.Add((Object)(_bomb));
 };
 };
 }
};
 //BA.debugLineNum = 314;BA.debugLine="For i = Bombs.Size - 1 To 0 Step -1";
{
final int step231 = (int) (-1);
final int limit231 = (int) (0);
for (_i = (int) (mostCurrent._bombs.getSize()-1); (step231 > 0 && _i <= limit231) || (step231 < 0 && _i >= limit231); _i = ((int)(0 + _i + step231))) {
 //BA.debugLineNum = 315;BA.debugLine="Dim Bomb As typBomb";
_bomb = new flm.b4a.spaceenemies.main._typbomb();
 //BA.debugLineNum = 316;BA.debugLine="Bomb = Bombs.Get(i)";
_bomb = (flm.b4a.spaceenemies.main._typbomb)(mostCurrent._bombs.Get(_i));
 //BA.debugLineNum = 317;BA.debugLine="If CollisionWithHero(Bomb.X, Bomb.X + bmpBomb.Width, Bomb.Y + Bit.ShiftRight(bmpBomb.Height, 1)) Then";
if (_collisionwithhero((int) (_bomb.X),(int) (_bomb.X+mostCurrent._bmpbomb.getWidth()),(int) (_bomb.Y+anywheresoftware.b4a.keywords.Common.Bit.ShiftRight(mostCurrent._bmpbomb.getHeight(),(int) (1))))) { 
 //BA.debugLineNum = 319;BA.debugLine="Hero.DyingState = 1";
mostCurrent._hero.DyingState = (short) (1);
 //BA.debugLineNum = 320;BA.debugLine="Lives = Lives - 1";
_lives = (byte) (_lives-1);
 //BA.debugLineNum = 321;BA.debugLine="SP.Play(HitSound, 1 - (Bomb.X / 100%x), Bomb.X / 100%x, 1, 0, 0.7)";
mostCurrent._sp.Play(_hitsound,(float) (1-(_bomb.X/(double)anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA))),(float) (_bomb.X/(double)anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA)),(int) (1),(int) (0),(float) (0.7));
 //BA.debugLineNum = 322;BA.debugLine="If Lives = 0 Then";
if (_lives==0) { 
 //BA.debugLineNum = 323;BA.debugLine="AnimateText(\"GAME OVER\")";
_animatetext("GAME OVER");
 }else {
 //BA.debugLineNum = 325;BA.debugLine="Bombs.RemoveAt(i)";
mostCurrent._bombs.RemoveAt(_i);
 };
 }else {
 //BA.debugLineNum = 328;BA.debugLine="Bomb.Y = Bomb.Y + (Secs * BombSpeed)";
_bomb.Y = (float) (_bomb.Y+(_secs*_bombspeed));
 //BA.debugLineNum = 329;BA.debugLine="If Bomb.InverseRotation Then";
if (_bomb.InverseRotation) { 
 //BA.debugLineNum = 330;BA.debugLine="Bomb.Angle = Bomb.Angle + (Secs * BombRotationSpeed)";
_bomb.Angle = (float) (_bomb.Angle+(_secs*_bombrotationspeed));
 }else {
 //BA.debugLineNum = 332;BA.debugLine="Bomb.Angle = Bomb.Angle - (Secs * BombRotationSpeed)";
_bomb.Angle = (float) (_bomb.Angle-(_secs*_bombrotationspeed));
 };
 //BA.debugLineNum = 334;BA.debugLine="If Bomb.Y > 100%y Then Bombs.RemoveAt(i)";
if (_bomb.Y>anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA)) { 
mostCurrent._bombs.RemoveAt(_i);};
 };
 }
};
 //BA.debugLineNum = 339;BA.debugLine="Hero.FireWait = Hero.FireWait - Elapsed";
mostCurrent._hero.FireWait = (long) (mostCurrent._hero.FireWait-_elapsed);
 //BA.debugLineNum = 340;BA.debugLine="For i = LaserShots.Size - 1 To 0 Step -1";
{
final int step254 = (int) (-1);
final int limit254 = (int) (0);
for (_i = (int) (mostCurrent._lasershots.getSize()-1); (step254 > 0 && _i <= limit254) || (step254 < 0 && _i >= limit254); _i = ((int)(0 + _i + step254))) {
 //BA.debugLineNum = 341;BA.debugLine="Dim LaserShot As typLaser";
_lasershot = new flm.b4a.spaceenemies.main._typlaser();
 //BA.debugLineNum = 342;BA.debugLine="LaserShot = LaserShots.Get(i)";
_lasershot = (flm.b4a.spaceenemies.main._typlaser)(mostCurrent._lasershots.Get(_i));
 //BA.debugLineNum = 343;BA.debugLine="If CollisionWithEnemy(LaserShot.X, LaserShot.X + 0.5%x, LaserShot.Y, LaserShot.Y + LaserShotSize) Then";
if (_collisionwithenemy((int) (_lasershot.X),(int) (_lasershot.X+anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0.5),mostCurrent.activityBA)),(int) (_lasershot.Y),(int) (_lasershot.Y+_lasershotsize))) { 
 //BA.debugLineNum = 345;BA.debugLine="SP.Play(HitSound, 1 - (LaserShot.X / 100%x), LaserShot.X / 100%x, 1, 0, 1)";
mostCurrent._sp.Play(_hitsound,(float) (1-(_lasershot.X/(double)anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA))),(float) (_lasershot.X/(double)anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA)),(int) (1),(int) (0),(float) (1));
 //BA.debugLineNum = 346;BA.debugLine="LaserShots.RemoveAt(i)";
mostCurrent._lasershots.RemoveAt(_i);
 //BA.debugLineNum = 347;BA.debugLine="Score = Score + 10 + (10 * Wave.Count)";
_score = (int) (_score+10+(10*mostCurrent._wave.Count));
 }else {
 //BA.debugLineNum = 349;BA.debugLine="LaserShot.Y = LaserShot.Y - (Secs * LaserSpeed)";
_lasershot.Y = (float) (_lasershot.Y-(_secs*_laserspeed));
 //BA.debugLineNum = 350;BA.debugLine="If LaserShot.Y < -LaserShotSize Then LaserShots.RemoveAt(i)";
if (_lasershot.Y<-_lasershotsize) { 
mostCurrent._lasershots.RemoveAt(_i);};
 };
 }
};
 //BA.debugLineNum = 353;BA.debugLine="End Sub";
return "";
}
public static String  _acsftext_draw(flm.b4a.accelerview.AcceleratedCanvas _ac) throws Exception{
 //BA.debugLineNum = 455;BA.debugLine="Sub AcSfText_Draw(AC As AS_Canvas)";
 //BA.debugLineNum = 457;BA.debugLine="If TextObj.IsInitialized Then";
if (mostCurrent._textobj.getIsInitialized()) { 
 //BA.debugLineNum = 458;BA.debugLine="AC.DrawTextObject(TextObj, Text, 0, 0, AC.ALIGN_CENTER)";
_ac.DrawTextObject(mostCurrent._textobj,mostCurrent._text,(float) (0),(float) (0),_ac.ALIGN_CENTER);
 };
 //BA.debugLineNum = 460;BA.debugLine="End Sub";
return "";
}
public static String  _activity_create(boolean _firsttime) throws Exception{
int _arrowsize = 0;
float _fontscale = 0f;
int _baseline = 0;
flm.b4a.accelerview.ComplexPath _pth = null;
anywheresoftware.b4a.phone.Phone _p = null;
boolean _honeycomb_ics = false;
 //BA.debugLineNum = 68;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 70;BA.debugLine="Activity.Title = \"Space Wars - Bermz ISWare Software Solutions\"";
mostCurrent._activity.setTitle((Object)("Space Wars - Bermz ISWare Software Solutions"));
 //BA.debugLineNum = 71;BA.debugLine="AcSf.Initialize(\"AcSf\", True)";
mostCurrent._acsf.Initialize(mostCurrent.activityBA,"AcSf",anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 72;BA.debugLine="AcSf.Color = Colors.Black";
mostCurrent._acsf.setColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 73;BA.debugLine="Activity.AddView(AcSf, 0, 0, 100%x, 100%y)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._acsf.getObject()),(int) (0),(int) (0),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA));
 //BA.debugLineNum = 76;BA.debugLine="BitmapSize = Sqrt(Power(4%x, 2) + Power(4%y, 2))";
_bitmapsize = (float) (anywheresoftware.b4a.keywords.Common.Sqrt(anywheresoftware.b4a.keywords.Common.Power(anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (4),mostCurrent.activityBA),2)+anywheresoftware.b4a.keywords.Common.Power(anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (4),mostCurrent.activityBA),2)));
 //BA.debugLineNum = 77;BA.debugLine="bmpDroid = IU.LoadScaledBitmap(File.DirAssets, \"alien.png\", BitmapSize, BitmapSize * 1.2, False)";
mostCurrent._bmpdroid.setObject((android.graphics.Bitmap)(mostCurrent._iu.LoadScaledBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"alien.png",(int) (_bitmapsize),(int) (_bitmapsize*1.2),anywheresoftware.b4a.keywords.Common.False)));
 //BA.debugLineNum = 78;BA.debugLine="bmpDroid2 = IU.LoadScaledBitmap(File.DirAssets, \"space_enemies/alien.png\", BitmapSize, BitmapSize * 1.2, False)";
mostCurrent._bmpdroid2.setObject((android.graphics.Bitmap)(mostCurrent._iu.LoadScaledBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"space_enemies/alien.png",(int) (_bitmapsize),(int) (_bitmapsize*1.2),anywheresoftware.b4a.keywords.Common.False)));
 //BA.debugLineNum = 81;BA.debugLine="bmpBomb = IU.LoadScaledBitmap(File.DirAssets, \"space_enemies/bomb.png\", BitmapSize / 2, BitmapSize / 2, False)";
mostCurrent._bmpbomb.setObject((android.graphics.Bitmap)(mostCurrent._iu.LoadScaledBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"space_enemies/bomb.png",(int) (_bitmapsize/(double)2),(int) (_bitmapsize/(double)2),anywheresoftware.b4a.keywords.Common.False)));
 //BA.debugLineNum = 84;BA.debugLine="bmpTux = IU.LoadScaledBitmap(File.DirAssets, \"space_enemies/tux.png\", BitmapSize, BitmapSize, False)";
mostCurrent._bmptux.setObject((android.graphics.Bitmap)(mostCurrent._iu.LoadScaledBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"space_enemies/tux.png",(int) (_bitmapsize),(int) (_bitmapsize),anywheresoftware.b4a.keywords.Common.False)));
 //BA.debugLineNum = 85;BA.debugLine="bmpMiniTux = IU.LoadScaledBitmap(File.DirAssets, \"space_enemies/tux.png\", BitmapSize * 0.7, BitmapSize * 0.7, False)";
mostCurrent._bmpminitux.setObject((android.graphics.Bitmap)(mostCurrent._iu.LoadScaledBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"space_enemies/tux.png",(int) (_bitmapsize*0.7),(int) (_bitmapsize*0.7),anywheresoftware.b4a.keywords.Common.False)));
 //BA.debugLineNum = 88;BA.debugLine="Dim ArrowSize As Int = Min(Max(50dip, BitmapSize * 1.2), 70dip)";
_arrowsize = (int) (anywheresoftware.b4a.keywords.Common.Min(anywheresoftware.b4a.keywords.Common.Max(anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (50)),_bitmapsize*1.2),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (70))));
 //BA.debugLineNum = 89;BA.debugLine="bmpALeft = IU.LoadScaledBitmap(File.DirAssets, \"space_enemies/arrow_left.jpg\", ArrowSize, ArrowSize, False)";
mostCurrent._bmpaleft.setObject((android.graphics.Bitmap)(mostCurrent._iu.LoadScaledBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"space_enemies/arrow_left.jpg",_arrowsize,_arrowsize,anywheresoftware.b4a.keywords.Common.False)));
 //BA.debugLineNum = 90;BA.debugLine="bmpARight = IU.LoadScaledBitmap(File.DirAssets, \"space_enemies/arrow_right.jpg\", ArrowSize, ArrowSize, False)";
mostCurrent._bmparight.setObject((android.graphics.Bitmap)(mostCurrent._iu.LoadScaledBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"space_enemies/arrow_right.jpg",_arrowsize,_arrowsize,anywheresoftware.b4a.keywords.Common.False)));
 //BA.debugLineNum = 93;BA.debugLine="SP.Initialize(8)";
mostCurrent._sp.Initialize((int) (8));
 //BA.debugLineNum = 94;BA.debugLine="LaserSound = SP.Load(File.DirAssets, \"space_enemies/laser.ogg\")";
_lasersound = mostCurrent._sp.Load(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"space_enemies/laser.ogg");
 //BA.debugLineNum = 95;BA.debugLine="HitSound = SP.Load(File.DirAssets, \"space_enemies/hit.ogg\")";
_hitsound = mostCurrent._sp.Load(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"space_enemies/hit.ogg");
 //BA.debugLineNum = 98;BA.debugLine="WaitBeforeNextBomb = 500 'milliseconds";
_waitbeforenextbomb = (int) (500);
 //BA.debugLineNum = 99;BA.debugLine="WaitBeforeDropping = 500 'milliseconds";
_waitbeforedropping = (int) (500);
 //BA.debugLineNum = 100;BA.debugLine="Wave.Count = 0";
mostCurrent._wave.Count = (int) (0);
 //BA.debugLineNum = 101;BA.debugLine="Wave.FireWait = WaitBeforeNextBomb";
mostCurrent._wave.FireWait = (long) (_waitbeforenextbomb);
 //BA.debugLineNum = 102;BA.debugLine="NextWave";
_nextwave();
 //BA.debugLineNum = 105;BA.debugLine="Bombs.Initialize";
mostCurrent._bombs.Initialize();
 //BA.debugLineNum = 106;BA.debugLine="BombSpeed = 35%y";
_bombspeed = (float) (anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (35),mostCurrent.activityBA));
 //BA.debugLineNum = 107;BA.debugLine="BombRotationSpeed = 2%x";
_bombrotationspeed = (float) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (2),mostCurrent.activityBA));
 //BA.debugLineNum = 108;BA.debugLine="LaserShots.Initialize";
mostCurrent._lasershots.Initialize();
 //BA.debugLineNum = 109;BA.debugLine="LaserSpeed = 40%y";
_laserspeed = (float) (anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (40),mostCurrent.activityBA));
 //BA.debugLineNum = 110;BA.debugLine="LaserShotSize = 4%y";
_lasershotsize = anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (4),mostCurrent.activityBA);
 //BA.debugLineNum = 113;BA.debugLine="Lives = 3";
_lives = (byte) (3);
 //BA.debugLineNum = 114;BA.debugLine="Hero.X = 50%x - (bmpTux.Width / 2)";
mostCurrent._hero.X = (float) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (50),mostCurrent.activityBA)-(mostCurrent._bmptux.getWidth()/(double)2));
 //BA.debugLineNum = 115;BA.debugLine="Hero.Y = 100%y - bmpTux.Height";
mostCurrent._hero.Y = (float) (anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA)-mostCurrent._bmptux.getHeight());
 //BA.debugLineNum = 116;BA.debugLine="Hero.ShipLeft = Hero.X - 1%x";
mostCurrent._hero.ShipLeft = (int) (mostCurrent._hero.X-anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (1),mostCurrent.activityBA));
 //BA.debugLineNum = 117;BA.debugLine="Hero.ShipRight = Hero.X + bmpTux.Width + 1%x";
mostCurrent._hero.ShipRight = (int) (mostCurrent._hero.X+mostCurrent._bmptux.getWidth()+anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (1),mostCurrent.activityBA));
 //BA.debugLineNum = 118;BA.debugLine="Hero.ShipCenter = (Hero.ShipLeft + Hero.ShipRight) / 2";
mostCurrent._hero.ShipCenter = (int) ((mostCurrent._hero.ShipLeft+mostCurrent._hero.ShipRight)/(double)2);
 //BA.debugLineNum = 119;BA.debugLine="Hero.ShipTop = 100%y - (Hero.ShipRight - Hero.ShipLeft)";
mostCurrent._hero.ShipTop = (int) (anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA)-(mostCurrent._hero.ShipRight-mostCurrent._hero.ShipLeft));
 //BA.debugLineNum = 120;BA.debugLine="Hero.FireWait = 0";
mostCurrent._hero.FireWait = (long) (0);
 //BA.debugLineNum = 121;BA.debugLine="Hero.Move = 0 '0 = Idle, -1 = Moving to the left, 1 = Moving to the right";
mostCurrent._hero.Move = (short) (0);
 //BA.debugLineNum = 122;BA.debugLine="Hero.DyingState = 0";
mostCurrent._hero.DyingState = (short) (0);
 //BA.debugLineNum = 123;BA.debugLine="WaitBeforeNextShot = 500 'milliseconds";
_waitbeforenextshot = (int) (500);
 //BA.debugLineNum = 126;BA.debugLine="Dim FontScale As Float = (100%x + 100%y) / (320dip + 480dip)";
_fontscale = (float) ((anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA)+anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA))/(double)(anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (320))+anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (480))));
 //BA.debugLineNum = 127;BA.debugLine="ScoreFontSize = 18 * FontScale";
_scorefontsize = (int) (18*_fontscale);
 //BA.debugLineNum = 128;BA.debugLine="ScoreHeight = 17dip * FontScale";
_scoreheight = (int) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (17))*_fontscale);
 //BA.debugLineNum = 131;BA.debugLine="Dim BaseLine As Int = 52%y";
_baseline = anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (52),mostCurrent.activityBA);
 //BA.debugLineNum = 132;BA.debugLine="Dim Pth As AS_Path";
_pth = new flm.b4a.accelerview.ComplexPath();
 //BA.debugLineNum = 133;BA.debugLine="Pth.Initialize(0, BaseLine + 20%y)";
_pth.Initialize((float) (0),(float) (_baseline+anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (20),mostCurrent.activityBA)));
 //BA.debugLineNum = 134;BA.debugLine="Pth.CubicTo(35%x, BaseLine, 65%x, BaseLine - 20%y, 100%x, BaseLine)";
_pth.CubicTo((float) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (35),mostCurrent.activityBA)),(float) (_baseline),(float) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (65),mostCurrent.activityBA)),(float) (_baseline-anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (20),mostCurrent.activityBA)),(float) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA)),(float) (_baseline));
 //BA.debugLineNum = 135;BA.debugLine="TextObj.CreateText(Typeface.DEFAULT_BOLD, True).SetColor(Colors.RGB(255, 100, 0))";
mostCurrent._textobj.CreateText((anywheresoftware.b4a.keywords.constants.TypefaceWrapper) anywheresoftware.b4a.AbsObjectWrapper.ConvertToWrapper(new anywheresoftware.b4a.keywords.constants.TypefaceWrapper(), (android.graphics.Typeface)(anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT_BOLD)),anywheresoftware.b4a.keywords.Common.True).SetColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (255),(int) (100),(int) (0)));
 //BA.debugLineNum = 136;BA.debugLine="TextObj.SetStyle(TextObj.STYLE_STROKE, 0.8%x).SetPath(Pth)";
mostCurrent._textobj.SetStyle(mostCurrent._textobj.STYLE_STROKE,anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0.8),mostCurrent.activityBA)).SetPath((Object)(_pth));
 //BA.debugLineNum = 140;BA.debugLine="Dim P As Phone";
_p = new anywheresoftware.b4a.phone.Phone();
 //BA.debugLineNum = 141;BA.debugLine="Dim Honeycomb_ICS As Boolean = (P.SdkVersion >= 11 AND P.SdkVersion <= 15)";
_honeycomb_ics = (_p.getSdkVersion()>=11 && _p.getSdkVersion()<=15);
 //BA.debugLineNum = 142;BA.debugLine="AcSfText.Initialize(\"AcSfText\", Not(Honeycomb_ICS))";
mostCurrent._acsftext.Initialize(mostCurrent.activityBA,"AcSfText",anywheresoftware.b4a.keywords.Common.Not(_honeycomb_ics));
 //BA.debugLineNum = 143;BA.debugLine="AcSfText.Color = Colors.Transparent";
mostCurrent._acsftext.setColor(anywheresoftware.b4a.keywords.Common.Colors.Transparent);
 //BA.debugLineNum = 144;BA.debugLine="Activity.AddView(AcSfText, 0, 0, 100%x, 100%y)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._acsftext.getObject()),(int) (0),(int) (0),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA));
 //BA.debugLineNum = 145;BA.debugLine="AnimateText(\"SPACE WARS\")";
_animatetext("SPACE WARS");
 //BA.debugLineNum = 146;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 194;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 195;BA.debugLine="AcSf.StopRegularDraw";
mostCurrent._acsf.StopRegularDraw();
 //BA.debugLineNum = 196;BA.debugLine="If UserClosed Then SP.Release";
if (_userclosed) { 
mostCurrent._sp.Release();};
 //BA.debugLineNum = 197;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 190;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 191;BA.debugLine="AcSf.StartRegularUpdateAndDraw(16)";
mostCurrent._acsf.StartRegularUpdateAndDraw(mostCurrent.activityBA,(int) (16));
 //BA.debugLineNum = 192;BA.debugLine="End Sub";
return "";
}
public static String  _animatetext(String _text2display) throws Exception{
int _i = 0;
anywheresoftware.b4a.objects.AnimationWrapper _animtext = null;
 //BA.debugLineNum = 166;BA.debugLine="Sub AnimateText(Text2Display As String)";
 //BA.debugLineNum = 168;BA.debugLine="Text = Text2Display";
mostCurrent._text = _text2display;
 //BA.debugLineNum = 169;BA.debugLine="For i = 30 To 200";
{
final int step115 = 1;
final int limit115 = (int) (200);
for (_i = (int) (30); (step115 > 0 && _i <= limit115) || (step115 < 0 && _i >= limit115); _i = ((int)(0 + _i + step115))) {
 //BA.debugLineNum = 170;BA.debugLine="MaxTextSize = i";
_maxtextsize = _i;
 //BA.debugLineNum = 171;BA.debugLine="TextObj.SetSize(MaxTextSize)";
mostCurrent._textobj.SetSize((float) (_maxtextsize));
 //BA.debugLineNum = 172;BA.debugLine="If TextObj.MeasureStringWidth(Text) > 80%x Then Exit";
if (mostCurrent._textobj.MeasureStringWidth(mostCurrent._text)>anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (80),mostCurrent.activityBA)) { 
if (true) break;};
 }
};
 //BA.debugLineNum = 174;BA.debugLine="TextToShow = True";
_texttoshow = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 175;BA.debugLine="AcSfText.Visible = True";
mostCurrent._acsftext.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 178;BA.debugLine="Dim AnimText As Animation";
_animtext = new anywheresoftware.b4a.objects.AnimationWrapper();
 //BA.debugLineNum = 179;BA.debugLine="AnimText.InitializeScaleCenter(\"AnimText\", 0, 0, 1, 1, AcSfText)";
_animtext.InitializeScaleCenter(mostCurrent.activityBA,"AnimText",(float) (0),(float) (0),(float) (1),(float) (1),(android.view.View)(mostCurrent._acsftext.getObject()));
 //BA.debugLineNum = 180;BA.debugLine="AnimText.Duration = 1500";
_animtext.setDuration((long) (1500));
 //BA.debugLineNum = 181;BA.debugLine="If Lives > 0 Then";
if (_lives>0) { 
 //BA.debugLineNum = 182;BA.debugLine="AnimText.RepeatMode = AnimText.REPEAT_REVERSE";
_animtext.setRepeatMode(_animtext.REPEAT_REVERSE);
 //BA.debugLineNum = 183;BA.debugLine="AnimText.RepeatCount = 1";
_animtext.setRepeatCount((int) (1));
 }else {
 //BA.debugLineNum = 185;BA.debugLine="AnimText.RepeatCount = 0";
_animtext.setRepeatCount((int) (0));
 };
 //BA.debugLineNum = 187;BA.debugLine="AnimText.Start(AcSfText)";
_animtext.Start((android.view.View)(mostCurrent._acsftext.getObject()));
 //BA.debugLineNum = 188;BA.debugLine="End Sub";
return "";
}
public static String  _animtext_animationend() throws Exception{
 //BA.debugLineNum = 462;BA.debugLine="Sub AnimText_AnimationEnd";
 //BA.debugLineNum = 464;BA.debugLine="If Lives > 0 Then";
if (_lives>0) { 
 //BA.debugLineNum = 465;BA.debugLine="AcSfText.Visible = False";
mostCurrent._acsftext.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 466;BA.debugLine="TextToShow = False";
_texttoshow = anywheresoftware.b4a.keywords.Common.False;
 }else {
 //BA.debugLineNum = 468;BA.debugLine="AcSf.StopRegularDraw";
mostCurrent._acsf.StopRegularDraw();
 };
 //BA.debugLineNum = 470;BA.debugLine="End Sub";
return "";
}
public static boolean  _collisionwithenemy(int _left,int _right,int _top,int _bottom) throws Exception{
int _x = 0;
int _y = 0;
int _i = 0;
flm.b4a.spaceenemies.main._typenemy _enemy = null;
 //BA.debugLineNum = 217;BA.debugLine="Sub CollisionWithEnemy(Left As Int, Right As Int, Top As Int, Bottom As Int) As Boolean";
 //BA.debugLineNum = 219;BA.debugLine="Dim X, Y As Int";
_x = 0;
_y = 0;
 //BA.debugLineNum = 220;BA.debugLine="For i = Enemies.Size - 1 To 0 Step -1";
{
final int step157 = (int) (-1);
final int limit157 = (int) (0);
for (_i = (int) (mostCurrent._enemies.getSize()-1); (step157 > 0 && _i <= limit157) || (step157 < 0 && _i >= limit157); _i = ((int)(0 + _i + step157))) {
 //BA.debugLineNum = 221;BA.debugLine="Dim Enemy As typEnemy";
_enemy = new flm.b4a.spaceenemies.main._typenemy();
 //BA.debugLineNum = 222;BA.debugLine="Enemy = Enemies.Get(i)";
_enemy = (flm.b4a.spaceenemies.main._typenemy)(mostCurrent._enemies.Get(_i));
 //BA.debugLineNum = 223;BA.debugLine="If Enemy.State < 2 Then";
if (_enemy.State<2) { 
 //BA.debugLineNum = 224;BA.debugLine="X = Wave.X + Enemy.OffsetX";
_x = (int) (mostCurrent._wave.X+_enemy.OffsetX);
 //BA.debugLineNum = 225;BA.debugLine="Y = Wave.Y + Enemy.OffsetY";
_y = (int) (mostCurrent._wave.Y+_enemy.OffsetY);
 //BA.debugLineNum = 226;BA.debugLine="If Left >= X AND X + bmpDroid.Width >= Right AND Top <= Y + bmpDroid.Height AND Bottom >= Y Then";
if (_left>=_x && _x+mostCurrent._bmpdroid.getWidth()>=_right && _top<=_y+mostCurrent._bmpdroid.getHeight() && _bottom>=_y) { 
 //BA.debugLineNum = 227;BA.debugLine="Enemy.State = 2 'First stage of death";
_enemy.State = (byte) (2);
 //BA.debugLineNum = 228;BA.debugLine="Return True";
if (true) return anywheresoftware.b4a.keywords.Common.True;
 };
 };
 }
};
 //BA.debugLineNum = 232;BA.debugLine="Return False";
if (true) return anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 233;BA.debugLine="End Sub";
return false;
}
public static boolean  _collisionwithhero(int _left,int _right,int _bottom) throws Exception{
 //BA.debugLineNum = 199;BA.debugLine="Sub CollisionWithHero(Left As Int, Right As Int, Bottom As Int) As Boolean";
 //BA.debugLineNum = 201;BA.debugLine="If Lives > 0 Then";
if (_lives>0) { 
 //BA.debugLineNum = 202;BA.debugLine="If Hero.ShipRight <= Left OR Hero.ShipLeft >= Right OR Bottom < Hero.ShipTop Then";
if (mostCurrent._hero.ShipRight<=_left || mostCurrent._hero.ShipLeft>=_right || _bottom<mostCurrent._hero.ShipTop) { 
 //BA.debugLineNum = 203;BA.debugLine="Return False";
if (true) return anywheresoftware.b4a.keywords.Common.False;
 }else {
 //BA.debugLineNum = 206;BA.debugLine="If Right < Hero.ShipCenter Then";
if (_right<mostCurrent._hero.ShipCenter) { 
 //BA.debugLineNum = 207;BA.debugLine="Return (Bottom - Hero.ShipTop - Bit.ShiftLeft(Hero.ShipCenter - Right, 1) > 0)";
if (true) return (_bottom-mostCurrent._hero.ShipTop-anywheresoftware.b4a.keywords.Common.Bit.ShiftLeft((int) (mostCurrent._hero.ShipCenter-_right),(int) (1))>0);
 }else if(_left>mostCurrent._hero.ShipCenter) { 
 //BA.debugLineNum = 209;BA.debugLine="Return (Bottom - Hero.ShipTop - Bit.ShiftLeft(Left - Hero.ShipCenter, 1) > 0)";
if (true) return (_bottom-mostCurrent._hero.ShipTop-anywheresoftware.b4a.keywords.Common.Bit.ShiftLeft((int) (_left-mostCurrent._hero.ShipCenter),(int) (1))>0);
 }else {
 //BA.debugLineNum = 211;BA.debugLine="Return True";
if (true) return anywheresoftware.b4a.keywords.Common.True;
 };
 };
 };
 //BA.debugLineNum = 215;BA.debugLine="End Sub";
return false;
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _globals() throws Exception{
 //BA.debugLineNum = 24;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 25;BA.debugLine="Dim AcSf As AcceleratedSurface";
mostCurrent._acsf = new flm.b4a.accelerview.AcceleratedSurface();
 //BA.debugLineNum = 26;BA.debugLine="Dim AcSfText As AcceleratedSurface";
mostCurrent._acsftext = new flm.b4a.accelerview.AcceleratedSurface();
 //BA.debugLineNum = 27;BA.debugLine="Dim IU As AS_ImageUtils";
mostCurrent._iu = new flm.b4a.accelerview.ImageUtils();
 //BA.debugLineNum = 29;BA.debugLine="Dim TextToShow As Boolean";
_texttoshow = false;
 //BA.debugLineNum = 30;BA.debugLine="Dim Text As String";
mostCurrent._text = "";
 //BA.debugLineNum = 31;BA.debugLine="Dim MaxTextSize As Int";
_maxtextsize = 0;
 //BA.debugLineNum = 32;BA.debugLine="Dim TextObj As AS_Text";
mostCurrent._textobj = new flm.b4a.accelerview.ComplexText();
 //BA.debugLineNum = 34;BA.debugLine="Dim Score As Int";
_score = 0;
 //BA.debugLineNum = 35;BA.debugLine="Dim ScoreFontSize As Int";
_scorefontsize = 0;
 //BA.debugLineNum = 36;BA.debugLine="Dim ScoreHeight As Int";
_scoreheight = 0;
 //BA.debugLineNum = 38;BA.debugLine="Dim Hero As typHero";
mostCurrent._hero = new flm.b4a.spaceenemies.main._typhero();
 //BA.debugLineNum = 39;BA.debugLine="Dim bmpTux, bmpMiniTux As Bitmap";
mostCurrent._bmptux = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();
mostCurrent._bmpminitux = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();
 //BA.debugLineNum = 40;BA.debugLine="Dim ShipPath As Path";
mostCurrent._shippath = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.PathWrapper();
 //BA.debugLineNum = 41;BA.debugLine="Dim Lives As Byte";
_lives = (byte)0;
 //BA.debugLineNum = 43;BA.debugLine="Dim Enemies As List";
mostCurrent._enemies = new anywheresoftware.b4a.objects.collections.List();
 //BA.debugLineNum = 44;BA.debugLine="Dim Wave As typWave";
mostCurrent._wave = new flm.b4a.spaceenemies.main._typwave();
 //BA.debugLineNum = 45;BA.debugLine="Dim bmpDroid, bmpDroid2 As Bitmap";
mostCurrent._bmpdroid = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();
mostCurrent._bmpdroid2 = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();
 //BA.debugLineNum = 46;BA.debugLine="Dim WaitBeforeDropping As Int";
_waitbeforedropping = 0;
 //BA.debugLineNum = 47;BA.debugLine="Dim WaitBeforeNextBomb As Int";
_waitbeforenextbomb = 0;
 //BA.debugLineNum = 49;BA.debugLine="Dim Bombs As List";
mostCurrent._bombs = new anywheresoftware.b4a.objects.collections.List();
 //BA.debugLineNum = 50;BA.debugLine="Dim BombSpeed As Float";
_bombspeed = 0f;
 //BA.debugLineNum = 51;BA.debugLine="Dim BombRotationSpeed As Float";
_bombrotationspeed = 0f;
 //BA.debugLineNum = 52;BA.debugLine="Dim bmpBomb As Bitmap";
mostCurrent._bmpbomb = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();
 //BA.debugLineNum = 54;BA.debugLine="Dim LaserShots As List";
mostCurrent._lasershots = new anywheresoftware.b4a.objects.collections.List();
 //BA.debugLineNum = 55;BA.debugLine="Dim LaserSpeed As Float";
_laserspeed = 0f;
 //BA.debugLineNum = 56;BA.debugLine="Dim LaserShotSize As Int";
_lasershotsize = 0;
 //BA.debugLineNum = 57;BA.debugLine="Dim WaitBeforeNextShot As Int";
_waitbeforenextshot = 0;
 //BA.debugLineNum = 59;BA.debugLine="Dim bmpALeft, bmpARight As Bitmap";
mostCurrent._bmpaleft = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();
mostCurrent._bmparight = new anywheresoftware.b4a.objects.drawable.CanvasWrapper.BitmapWrapper();
 //BA.debugLineNum = 60;BA.debugLine="Dim BitmapSize As Float";
_bitmapsize = 0f;
 //BA.debugLineNum = 61;BA.debugLine="Dim Elapsed As Long";
_elapsed = 0L;
 //BA.debugLineNum = 63;BA.debugLine="Dim SP As SoundPool";
mostCurrent._sp = new anywheresoftware.b4a.audio.SoundPoolWrapper();
 //BA.debugLineNum = 64;BA.debugLine="Dim LaserSound As Int";
_lasersound = 0;
 //BA.debugLineNum = 65;BA.debugLine="Dim HitSound As Int";
_hitsound = 0;
 //BA.debugLineNum = 66;BA.debugLine="End Sub";
return "";
}
public static String  _nextwave() throws Exception{
int _x = 0;
int _y = 0;
flm.b4a.spaceenemies.main._typenemy _enemy = null;
 //BA.debugLineNum = 148;BA.debugLine="Sub NextWave";
 //BA.debugLineNum = 150;BA.debugLine="Enemies.Initialize";
mostCurrent._enemies.Initialize();
 //BA.debugLineNum = 151;BA.debugLine="For X = 0 To 9";
{
final int step99 = 1;
final int limit99 = (int) (9);
for (_x = (int) (0); (step99 > 0 && _x <= limit99) || (step99 < 0 && _x >= limit99); _x = ((int)(0 + _x + step99))) {
 //BA.debugLineNum = 152;BA.debugLine="For Y = 0 To 3";
{
final int step100 = 1;
final int limit100 = (int) (3);
for (_y = (int) (0); (step100 > 0 && _y <= limit100) || (step100 < 0 && _y >= limit100); _y = ((int)(0 + _y + step100))) {
 //BA.debugLineNum = 153;BA.debugLine="Dim Enemy As typEnemy";
_enemy = new flm.b4a.spaceenemies.main._typenemy();
 //BA.debugLineNum = 154;BA.debugLine="Enemy.OffsetX = X * BitmapSize * 1.2";
_enemy.OffsetX = (int) (_x*_bitmapsize*1.2);
 //BA.debugLineNum = 155;BA.debugLine="Enemy.OffsetY = Y * BitmapSize * 1.3";
_enemy.OffsetY = (int) (_y*_bitmapsize*1.3);
 //BA.debugLineNum = 156;BA.debugLine="Enemy.State = 0 '0 = Idle, 1 = Firing, 2+ = Dying";
_enemy.State = (byte) (0);
 //BA.debugLineNum = 157;BA.debugLine="Enemy.FireWait = WaitBeforeDropping";
_enemy.FireWait = (long) (_waitbeforedropping);
 //BA.debugLineNum = 158;BA.debugLine="Enemies.Add(Enemy)";
mostCurrent._enemies.Add((Object)(_enemy));
 }
};
 }
};
 //BA.debugLineNum = 161;BA.debugLine="Wave.X = 1%x";
mostCurrent._wave.X = (float) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (1),mostCurrent.activityBA));
 //BA.debugLineNum = 162;BA.debugLine="Wave.Y = 1%y";
mostCurrent._wave.Y = (float) (anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (1),mostCurrent.activityBA));
 //BA.debugLineNum = 163;BA.debugLine="Wave.Speed = 5%x + (1.5%x * Wave.Count)";
mostCurrent._wave.Speed = (float) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (5),mostCurrent.activityBA)+(anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (1.5),mostCurrent.activityBA)*mostCurrent._wave.Count));
 //BA.debugLineNum = 164;BA.debugLine="End Sub";
return "";
}
public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 15;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 16;BA.debugLine="Type typHero(X As Float, Y As Float, ShipLeft As Int, ShipRight As Int, ShipCenter As Int, ShipTop As Int, _ 	             FireWait As Long, Move As Short, DyingState As Short)";
;
 //BA.debugLineNum = 18;BA.debugLine="Type typEnemy(OffsetX As Int, OffsetY As Int, State As Byte, FireWait As Long)";
;
 //BA.debugLineNum = 19;BA.debugLine="Type typWave(Count As Int, X As Float, Y As Float, Speed As Float, FireWait As Long)";
;
 //BA.debugLineNum = 20;BA.debugLine="Type typBomb(X As Float, Y As Float, Angle As Float, InverseRotation As Boolean)";
;
 //BA.debugLineNum = 21;BA.debugLine="Type typLaser(X As Float, Y As Float)";
;
 //BA.debugLineNum = 22;BA.debugLine="End Sub";
return "";
}
}
