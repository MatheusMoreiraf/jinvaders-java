package org.opensourcearcade.jinvaders;

import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

public class SoundClip
{
	private AudioFormat af;
	private int size;
	private byte[] audio;
	private DataLine.Info info;
	private boolean loaded;
	private String name;

	public SoundClip(URL url)
	{
		try
		{
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
			this.af = audioInputStream.getFormat();
			this.size = (int) (af.getFrameSize() * audioInputStream.getFrameLength());
			this.audio = new byte[size];
			audioInputStream.read(audio, 0, size);
			this.info = new DataLine.Info(Clip.class, af, size);
			this.name = url.getFile();
			loaded=true;
		}
		catch (Exception e)
		{
			System.err.println("Unable to load "+name);
		}
	}

	public void play()
	{
		if(loaded)
		{
			try
			{
				Clip clip = (Clip) AudioSystem.getLine(info);
				clip.open(af, audio, 0, size);
				clip.start();
			}
			catch (Exception e)
			{
				System.err.println(e.getLocalizedMessage()+" "+name);
			}
		}
	}
}
