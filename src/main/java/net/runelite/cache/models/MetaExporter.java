package net.runelite.cache.models;

import net.runelite.cache.definitions.ModelDefinition;

import java.io.PrintWriter;

public class MetaExporter
{
	public static void export(ModelDefinition model, PrintWriter metaWriter)
	{
		if (model.vertexSkins != null)
		{
			for (int index = 0; index < model.vertexSkins.length; index++)
			{
				metaWriter.println("vg " + index + " " + model.vertexSkins[index]);
			}
		}

		if (model.faceSkins != null)
		{
			for (int index = 0; index < model.faceSkins.length; index++)
			{
				metaWriter.println("fs " + index + " " + model.faceSkins[index]);
			}
		}

		if (model.faceRenderPriorities != null)
		{
			for (int index = 0; index < model.faceRenderPriorities.length; index++)
			{
				metaWriter.println("fp " + index + " " + model.faceRenderPriorities[index]);
			}
		}
	}
}
