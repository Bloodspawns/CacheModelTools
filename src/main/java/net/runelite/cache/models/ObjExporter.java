/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.cache.models;

import java.awt.Color;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.ArrayListMultimap;
import lombok.extern.slf4j.Slf4j;
import net.runelite.cache.Material;
import net.runelite.cache.TextureManager;
import net.runelite.cache.definitions.ModelDefinition;
import net.runelite.cache.definitions.TextureDefinition;

@Slf4j
public class ObjExporter
{
	private final TextureManager textureManager;
	private final ModelDefinition model;

	public ObjExporter(TextureManager textureManager, ModelDefinition model)
	{
		this.textureManager = textureManager;
		this.model = model;
	}

	public void export(PrintWriter objWriter, PrintWriter mtlWriter)
	{
		model.computeNormals();

		ArrayListMultimap<Material, Integer> materials = getMaterials(model);
		HashMap<Integer, Material> faceMaterials = new HashMap<>();
		if (materials != null && materials.keys() != null)
		{
			var keys = materials.keySet().toArray(new Material[0]);
			for (int i = 0; i < keys.length; i++)
			{
				var key = keys[i];
				key.name = "m." + i;

				mtlWriter.println("newmtl " + key.name);
				mtlWriter.println("Kd " + key.kd_r + " " + key.kd_g + " " + key.kd_b);
				mtlWriter.println("d " + key.d);
				mtlWriter.println();

				for (Integer index : materials.get(key))
				{
					faceMaterials.put(index, key);
				}
			}
		}

		objWriter.println("mtllib " + model.id + ".mtl");

		objWriter.println("o runescapemodel");

		for (int i = 0; i < model.vertexCount; ++i)
		{
			objWriter.println("v " + model.vertexPositionsX[i] + " "
					+ model.vertexPositionsY[i] * -1 + " "
					+ model.vertexPositionsZ[i] * -1);
		}

		if (model.faceTextures != null)
		{
			float[][] u = model.faceTextureUCoordinates;
			float[][] v = model.faceTextureVCoordinates;

			for (int i = 0; i < model.faceCount; ++i)
			{
				objWriter.println("vt " + u[i][0] + " " + v[i][0]);
				objWriter.println("vt " + u[i][1] + " " + v[i][1]);
				objWriter.println("vt " + u[i][2] + " " + v[i][2]);
			}
		}

		for (VertexNormal normal : model.vertexNormals)
		{
			objWriter.println("vn " + normal.x + " " + normal.y + " " + normal.z);
		}

		Material lastMaterial = null;
		for (int i = 0; i < model.faceCount; ++i)
		{
			int x = model.faceVertexIndices1[i] + 1;
			int y = model.faceVertexIndices2[i] + 1;
			int z = model.faceVertexIndices3[i] + 1;

			Material material = faceMaterials.get(i);
			if (!material.equals(lastMaterial))
			{
				objWriter.println("usemtl " + material.name);
				lastMaterial = material;
			}
			if (model.faceTextures != null)
			{
				objWriter.println("f "
						+ x + "/" + (i * 3 + 1) + " "
						+ y + "/" + (i * 3 + 2) + " "
						+ z + "/" + (i * 3 + 3));
			}
			else
			{
				objWriter.println("f " + x + " " + y + " " + z);
			}
		}
	}

	private static ArrayListMultimap<Material, Integer> getMaterials(ModelDefinition model)
	{
		ArrayListMultimap<Material, Integer> materials = ArrayListMultimap.create();

		for (int i = 0; i < model.faceCount; ++i)
		{
			if (model.faceTextures != null)
			{
				log.warn("Obj Exporter does not support textures at this point");
				return null;
			}

			var material = Material.fromFaceIndex(model, i);

			materials.put(material, i);
		}

		return materials;
	}

	private static Color rs2hsbToColor(int hsb)
	{
		int decode_hue = (hsb >> 10) & 0x3f;
		int decode_saturation = (hsb >> 7) & 0x07;
		int decode_brightness = (hsb & 0x7f);
		return Color.getHSBColor((float) decode_hue / 63, (float) decode_saturation / 7, (float) decode_brightness / 127);
	}
}
