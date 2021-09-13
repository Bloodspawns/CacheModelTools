package net.runelite.cache.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.cache.Material;
import net.runelite.cache.definitions.ModelDefinition;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
public class ObjImporter
{
	@Data
	@AllArgsConstructor
	static class Vertex
	{
		int x;
		int y;
		int z;
	}

	@Data
	@AllArgsConstructor
	static class Triangle
	{
		String material;
		int index1;
		int index2;
		int index3;
	}

	public static ModelDefinition fromObj(File obj, File mtl, File meta)
	{
		ModelDefinition model = new ModelDefinition();
		ArrayList<Vertex> vertices = new ArrayList<>();
		ArrayList<Triangle> triangles = new ArrayList<>();
		HashMap<String, Material> materials = new HashMap<>();

		boolean hasAlphas = false;
		try (var mtlStream = new BufferedReader(new FileReader(mtl)))
		{
			String line;
			Material lastMaterial = null;
			while ((line = mtlStream.readLine()) != null)
			{
				if (line.isBlank() || line.isEmpty())
				{
					continue;
				}

				line = line.trim();

				if (line.startsWith("newmtl"))
				{
					if (lastMaterial != null)
					{
						materials.put(lastMaterial.name, lastMaterial);
					}
					String name = line.split("\\s")[1];
					lastMaterial = new Material();
					lastMaterial.name = name;
				}
				else if (line.startsWith("Kd "))
				{
					if (lastMaterial == null)
					{
						throw new RuntimeException("Headless material");
					}
					String[] split = line.split("\\s");
					double r = Double.parseDouble(split[1]);
					double g = Double.parseDouble(split[2]);
					double b = Double.parseDouble(split[3]);
					lastMaterial.kd_r = r;
					lastMaterial.kd_g = g;
					lastMaterial.kd_b = b;
				}
				else if (line.startsWith("d "))
				{
					if (lastMaterial == null)
					{
						throw new RuntimeException("Headless material");
					}
					String[] split = line.split("\\s");
					double d = Double.parseDouble(split[1]);
					if (d != 1.0)
					{
						hasAlphas = true;
						lastMaterial.d = d;
					}
				}
			}
			if (lastMaterial != null)
			{
				materials.put(lastMaterial.name, lastMaterial);
			}
		}
		catch (IOException io)
		{
			log.warn("Error reading mtl file", io);
			return null;
		}

		try (var objStream = new BufferedReader(new FileReader(obj)))
		{
			String line;
			String lastMaterial = null;
			while ((line = objStream.readLine()) != null)
			{
				if (line.isBlank() || line.isEmpty())
				{
					continue;
				}

				line = line.trim();

				if (line.startsWith("o ") || line.startsWith("mtllib ") || line.startsWith("vn "))
				{
					continue;
				}

				if (line.startsWith("v "))
				{
					String[] data = line.split("\\s");
					int x = Math.round(Float.parseFloat(data[1]));
					int y = Math.round(Float.parseFloat(data[2]));
					int z = Math.round(Float.parseFloat(data[3]));
					Vertex vertex = new Vertex(x, y, z);
					vertices.add(vertex);
				}
				else if (line.startsWith("usemtl "))
				{
					lastMaterial = line.split("\\s")[1];
				}
				else if (line.startsWith("f "))
				{
					String[] data = line.split("\\s");
					if (data.length > 4)
					{
						throw new RuntimeException("Only triangles are supported");
					}
					String f1 = data[1];
					String f2 = data[2];
					String f3 = data[3];
					int index;
					if ((index = f1.indexOf("/")) != -1)
					{
						f1 = f1.substring(0, index);
					}
					if ((index = f2.indexOf("/")) != -1)
					{
						f2 = f2.substring(0, index);
					}
					if ((index = f3.indexOf("/")) != -1)
					{
						f3 = f3.substring(0, index);
					}
					int index1 = Integer.parseInt(f1) - 1;
					int index2 = Integer.parseInt(f2) - 1;
					int index3 = Integer.parseInt(f3) - 1;
					Triangle triangle = new Triangle(lastMaterial, index1, index2, index3);
					triangles.add(triangle);
				}
				else
				{
					log.warn("Missing implementation for parsing {}", line);
				}
			}
		}
		catch (IOException io)
		{
			log.warn("Error reading mtl file", io);
			return null;
		}

		if (meta != null)
		{
			try (var metaStream = new BufferedReader(new FileReader(meta)))
			{
				String line;
				while ((line = metaStream.readLine()) != null)
				{
					if (line.startsWith("vg "))
					{
						if (model.vertexSkins == null)
						{
							model.vertexSkins = new int[vertices.size()];
						}
						var split = line.split("\\s");
						int index = Integer.parseInt(split[1]);
						int group = Integer.parseInt(split[2]);
						model.vertexSkins[index] = group;
					}
					else if (line.startsWith("fs "))
					{
						if (model.faceSkins == null)
						{
							model.faceSkins = new int[triangles.size()];
						}
						var split = line.split("\\s");
						int index = Integer.parseInt(split[1]);
						int skin = Integer.parseInt(split[2]);
						model.faceSkins[index] = skin;
					}
					else if (line.startsWith("fp "))
					{
						if (model.faceRenderPriorities == null)
						{
							model.faceRenderPriorities = new byte[triangles.size()];
						}
						var split = line.split("\\s");
						int index = Integer.parseInt(split[1]);
						byte priority = Byte.parseByte(split[2]);
						model.faceRenderPriorities[index] = priority;
					}
				}
			}
			catch (IOException io)
			{
				log.warn("Error reading meta file", io);
				return null;
			}
		}

		int faceCount = triangles.size();
		int vertexCount = vertices.size();
		model.faceCount = faceCount;
		model.vertexCount = vertexCount;
		model.faceColors = new short[faceCount];
		model.faceVertexIndices1 = new int[faceCount];
		model.faceVertexIndices2 = new int[faceCount];
		model.faceVertexIndices3 = new int[faceCount];
		model.vertexPositionsX = new int[vertexCount];
		model.vertexPositionsY = new int[vertexCount];
		model.vertexPositionsZ = new int[vertexCount];

		if (model.faceSkins == null)
		{
			model.faceSkins = new int[faceCount];
		}

		if (model.faceRenderPriorities == null)
		{
			model.faceRenderPriorities = new byte[faceCount];
			for (int i = 0; i < faceCount; i++)
			{
				model.faceRenderPriorities[i] = 8;
			}
		}

		if (hasAlphas && model.faceAlphas == null)
		{
			model.faceAlphas = new byte[faceCount];
		}

		for (int i = 0; i < faceCount; i++)
		{
			Triangle triangle = triangles.get(i);
			model.faceVertexIndices1[i] = triangle.getIndex1();
			model.faceVertexIndices2[i] = triangle.getIndex2();
			model.faceVertexIndices3[i] = triangle.getIndex3();

			Material material = materials.get(triangle.material);
			int r = (int) Math.round(material.getKd_r() * 255.0);
			int g = (int) Math.round(material.getKd_g() * 255.0);
			int b = (int) Math.round(material.getKd_b() * 255.0);
			short color = rs2ColorToHsb(r, g, b);
			model.faceColors[i] = color;

			if (model.faceAlphas != null)
			{
				model.faceAlphas[i] = (byte)Math.min(Byte.MAX_VALUE, Math.round(material.d * Byte.MAX_VALUE));
			}
		}

		for (int i = 0; i < vertexCount; i++)
		{
			Vertex vertex = vertices.get(i);
			model.vertexPositionsX[i] = vertex.x;
			model.vertexPositionsY[i] = vertex.y * - 1;
			model.vertexPositionsZ[i] = vertex.z * - 1;
		}

		return model;
	}

	public static short rs2ColorToHsb(Color color)
	{
		return rs2ColorToHsb(color.getRed(), color.getGreen(), color.getBlue());
	}

	public static short rs2ColorToHsb(int r, int g, int b)
	{
		float[] values = Color.RGBtoHSB(r, g, b, null);
		int hue = Math.round(values[0] * 63);
		int saturation = Math.round(values[1] * 7);
		int brightness = Math.round(values[2] * 127);
		return (short)((hue << 10) | (saturation << 7) | (brightness));
	}

	public static Color rs2hsbToColor(int hsb)
	{
		int decode_hue = (hsb >> 10) & 0x3f;
		int decode_saturation = (hsb >> 7) & 0x07;
		int decode_brightness = (hsb & 0x7f);
		return Color.getHSBColor((float) decode_hue / 63, (float) decode_saturation / 7, (float) decode_brightness / 127);
	}
}
