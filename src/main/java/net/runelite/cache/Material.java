package net.runelite.cache;


import lombok.Data;
import net.runelite.cache.definitions.ModelDefinition;

import java.awt.Color;
import java.util.Objects;

import static net.runelite.cache.models.ObjImporter.rs2hsbToColor;

@Data
public class Material
{
	public String name;
	public double kd_r;
	public double kd_g;
	public double kd_b;
	public double d;

	@Override
	public boolean equals(Object o)
	{
		if (o == this) return true;
		if (!(o instanceof Material)) {
			return false;
		}
		Material m = (Material) o;
		return Objects.equals(kd_r, m.kd_r) && Objects.equals(kd_g, m.kd_g) &&
				Objects.equals(kd_b, m.kd_b) && Objects.equals(d, m.d);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(kd_r, kd_g, kd_b, d);
	}

	public static Material fromFaceIndex(ModelDefinition model, int index)
	{
		Material material = new Material();

		Color color = rs2hsbToColor(model.faceColors[index]);

		double r = color.getRed() / 255.0;
		double g = color.getGreen() / 255.0;
		double b = color.getBlue() / 255.0;
		material.kd_r = r;
		material.kd_g = g;
		material.kd_b = b;

		if (model.faceAlphas != null)
		{
			material.d = Math.max(0.0, Math.min(1.0, model.faceAlphas[index] / (double)Byte.MAX_VALUE));
		}
		else
		{
			material.d = 1;
		}

		return material;
	}
}