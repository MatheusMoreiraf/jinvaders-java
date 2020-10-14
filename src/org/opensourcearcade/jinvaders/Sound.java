package org.opensourcearcade.jinvaders;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.FileNotFoundException;
import java.net.URL;

public final class Sound {

	private Sound() {};

	private static boolean enabled;

	public static enum SOUNDS {
		SHOT, PLY_HIT, INV_HIT, BASE_HIT, UFO, UFO_HIT, WALK1, WALK2, WALK3, WALK4
	};

	private static AudioClip[] clips = new AudioClip[SOUNDS.values().length];

	public static void loadSound(SOUNDS index, URL url) {
		try {
			clips[index.ordinal()] = Applet.newAudioClip(url);
		}
		catch (Exception e) {
			System.err.println("Sound: "+e.getMessage());
		}
	}

	public static void play(SOUNDS index) {
		if(enabled) clips[index.ordinal()].play();
	}

	public static void play(int index) {
		if (enabled&&index>0&&index<SOUNDS.values().length) clips[index].play();
	}

	public static void loop(SOUNDS index) {
		if (enabled) clips[index.ordinal()].loop();
	}

	public static void stop(SOUNDS index) {
		clips[index.ordinal()].stop();
	}

	public static void setEnabled(boolean enabled) {
		Sound.enabled = enabled;

		if (!enabled) {
			for (int i = 0; i<clips.length; i++)
				if (clips[i]!=null) clips[i].stop();
		}
	}

	static void init() throws FileNotFoundException {
		loadSound(SOUNDS.SHOT, ToolBox.getURL("sounds/PlyShot_44.wav"));
		loadSound(SOUNDS.PLY_HIT, ToolBox.getURL("sounds/PlyHit_44.wav"));
		loadSound(SOUNDS.INV_HIT, ToolBox.getURL("sounds/InvHit_44.wav"));
		loadSound(SOUNDS.BASE_HIT, ToolBox.getURL("sounds/BaseHit_44.wav"));
		loadSound(SOUNDS.UFO, ToolBox.getURL("sounds/Ufo.wav"));
		loadSound(SOUNDS.UFO_HIT, ToolBox.getURL("sounds/UfoHit.wav"));
		loadSound(SOUNDS.WALK1, ToolBox.getURL("sounds/Walk1.wav"));
		loadSound(SOUNDS.WALK2, ToolBox.getURL("sounds/Walk2.wav"));
		loadSound(SOUNDS.WALK3, ToolBox.getURL("sounds/Walk3.wav"));
		loadSound(SOUNDS.WALK4, ToolBox.getURL("sounds/Walk4.wav"));
		setEnabled(true);
	}

	public static boolean isEnabled() {
		return enabled;
	}
}
