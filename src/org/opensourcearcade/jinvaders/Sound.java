package org.opensourcearcade.jinvaders;

import java.applet.Applet;
import java.applet.AudioClip;
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

	public static boolean isEnabled() {
		return enabled;
	}
}
