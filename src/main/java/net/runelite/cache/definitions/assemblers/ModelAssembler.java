package net.runelite.cache.definitions.assemblers;

import lombok.extern.slf4j.Slf4j;
import net.runelite.cache.definitions.ModelDefinition;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

@Slf4j
public class ModelAssembler
{
	// This will create a model of type 1 (there is also a type 2 model for rs2)
	public static byte[] AssembleModel(ModelDefinition model)
	{
		int vertexCount = model.vertexCount;
		int faceCount = model.faceCount;

		ArrayList<Integer> vertexFlags = new ArrayList<>();
		ArrayList<Integer> xVertices = new ArrayList<>();
		ArrayList<Integer> yVertices = new ArrayList<>();
		ArrayList<Integer> zVertices = new ArrayList<>();
		createRs2Vertices(model, vertexFlags, xVertices, yVertices, zVertices);
		boolean validVertices = validateRs2Vertices(model, vertexFlags, xVertices, yVertices, zVertices);
		log.debug("Valid vertices {}", validVertices);

		ArrayList<Integer> indexTypes = new ArrayList<>();
		ArrayList<Integer> indices = new ArrayList<>();
		createRs2Indices(model, indexTypes, indices);
		boolean validIndices = validateRs2Indices(model, indexTypes, indices);
		log.debug("Valid indices {}", validIndices);

		var out = new ByteArrayOutputStream();
		DataOutputStream outputStream = new DataOutputStream(out);

		int lastSize;
		try
		{
			lastSize = out.size();
			log.info("vertexFlags position {}", lastSize);
			for (Integer vertexFlag : vertexFlags)
			{
				outputStream.writeByte(vertexFlag);
			}
			int vertexFlagsByteCount = out.size() - lastSize;

			lastSize = out.size();
			log.info("indexTypes position {}", lastSize);
			for (Integer indexType : indexTypes)
			{
				outputStream.writeByte(indexType);
			}
			int indexTypesByteCount = out.size() - lastSize;

			lastSize = out.size();
			log.info("face priority position {}", lastSize);
			for (byte faceRenderPriority : model.faceRenderPriorities)
			{
				outputStream.writeByte(faceRenderPriority);
			}
			int faceRenderPriorityByteCount = out.size() - lastSize;

			lastSize = out.size();
			log.info("face skin position {}", lastSize);
			for (int faceSkin : model.faceSkins)
			{
				outputStream.writeByte(faceSkin);
			}

			lastSize = out.size();
			log.info("vertex skins position {}", lastSize);
			for (int vertexSkin : model.vertexSkins)
			{
				outputStream.writeByte(vertexSkin);
			}

			if (model.faceAlphas != null)
			{
				lastSize = out.size();
				log.info("Face alphas position {}", lastSize);
				for (byte faceAlpha : model.faceAlphas)
				{
					outputStream.writeByte(faceAlpha);
				}
				int faceAlphasByteCount = out.size() - lastSize;
			}


			lastSize = out.size();
			log.info("indices position {}", lastSize);
			for (Integer index : indices)
			{
				writeShortSmart(outputStream, index);
			}
			log.info("indices count {}", indices.size());
			int indicesByteCount = out.size() - lastSize;

			lastSize = out.size();
			log.info("face color position {}", lastSize);
			for (short faceColor : model.faceColors)
			{
				outputStream.writeShort(faceColor);
			}
			int faceColorByteCount = out.size() - lastSize;

			lastSize = out.size();
			log.info("x vertices position {}", lastSize);
			for (Integer v : xVertices)
			{
				writeShortSmart(outputStream, v);
			}
			int xVerticesByteCount = out.size() - lastSize;

			lastSize = out.size();
			log.info("y vertices position {}", lastSize);
			for (Integer v : yVertices)
			{
				writeShortSmart(outputStream, v);
			}
			int yVerticesByteCount = out.size() - lastSize;

			lastSize = out.size();
			log.info("z vertices position {}", lastSize);
			for (Integer v : zVertices)
			{
				writeShortSmart(outputStream, v);
			}
			int zVerticesByteCount = out.size() - lastSize;

			outputStream.writeByte(0);

			lastSize = out.size();
			log.info("footer position {}", lastSize);
			//footer
			outputStream.writeShort(vertexCount);
			outputStream.writeShort(faceCount);
			outputStream.writeByte(model.textureTriangleCount);
			outputStream.writeByte(model.faceRenderTypes == null ? 0 : 1);
			outputStream.writeByte(model.faceRenderPriorities == null ? 0 : 255);
			outputStream.writeByte(model.faceAlphas == null ? 0 : 1);
			outputStream.writeByte(model.faceSkins == null ? 0 : 1);
			outputStream.writeByte(model.faceTextures == null ? 0 : 1);
			outputStream.writeByte(model.vertexSkins == null ? 0 : 1);
			outputStream.writeShort(xVerticesByteCount);
			outputStream.writeShort(yVerticesByteCount);
			outputStream.writeShort(zVerticesByteCount);
			outputStream.writeShort(indicesByteCount);
			outputStream.writeShort(model.textureCoordinates == null ? 0 : model.textureCoordinates.length);
			outputStream.writeByte(-1);
			outputStream.writeByte(-1);
			out.close();
			outputStream.close();

		}
		catch (IOException e)
		{
			log.warn("Error writing to buffer", e);
		}

		return out.toByteArray();
	}

	public static void createRs2Vertices(ModelDefinition model, ArrayList<Integer> vertexFlags, ArrayList<Integer> xVertices,
			ArrayList<Integer> yVertices, ArrayList<Integer> zVertices)
	{
		int vertexCount = model.vertexCount;
		int lastX = 0;
		int lastY = 0;
		int lastZ = 0;

		for (int i = 0; i < vertexCount; i++)
		{
			int x = model.vertexPositionsX[i];
			int y = model.vertexPositionsY[i];
			int z = model.vertexPositionsZ[i];

			int flag = 0;
			if (x != lastX)
			{
				flag |= 1;
				xVertices.add(x - lastX);
			}
			if (y != lastY)
			{
				flag |= 2;
				yVertices.add(y - lastY);
			}
			if (z != lastZ)
			{
				flag |= 4;
				zVertices.add(z - lastZ);
			}

			lastX = x;
			lastY = y;
			lastZ = z;
			vertexFlags.add(flag);
		}
	}

	private static boolean validateRs2Vertices(ModelDefinition model, ArrayList<Integer> vFlags, ArrayList<Integer> xVertices,
											   ArrayList<Integer> yVertices, ArrayList<Integer> zVertices)
	{
		int vertexCount = model.vertexCount;
		Queue<Integer> flags = new LinkedList<>(vFlags);
		Queue<Integer> xs = new LinkedList<>(xVertices);
		Queue<Integer> ys = new LinkedList<>(yVertices);
		Queue<Integer> zs = new LinkedList<>(zVertices);

		int[] xvs = new int[vertexCount];
		int[] yvs = new int[vertexCount];
		int[] zvs = new int[vertexCount];

		int lastVertexX = 0;
		int lastVertexY = 0;
		int lastVertexZ = 0;

		int point;
		for (point = 0; point < vertexCount; ++point)
		{
			int preX = lastVertexX;
			int preY = lastVertexY;
			int preZ = lastVertexZ;

			int vertexFlags = flags.poll();
			int vertexXOffset = 0;
			if ((vertexFlags & 1) != 0)
			{
				vertexXOffset = xs.poll();
			}

			int vertexYOffset = 0;
			if ((vertexFlags & 2) != 0)
			{
				vertexYOffset = ys.poll();
			}

			int vertexZOffset = 0;
			if ((vertexFlags & 4) != 0)
			{
				vertexZOffset = zs.poll();
			}

			xvs[point] = lastVertexX + vertexXOffset;
			yvs[point] = lastVertexY + vertexYOffset;
			zvs[point] = lastVertexZ + vertexZOffset;
			lastVertexX = xvs[point];
			lastVertexY = yvs[point];
			lastVertexZ = zvs[point];

			boolean xEquals = xvs[point] == model.vertexPositionsX[point];
			boolean yEquals = yvs[point] == model.vertexPositionsY[point];
			boolean zEquals = zvs[point] == model.vertexPositionsZ[point];

			if (!xEquals || !yEquals || !zEquals)
			{
				return false;
			}
		}


		boolean xEqual = true;
		for (int i = 0; i < xvs.length; i++)
		{
			if (model.vertexPositionsX[i] != xvs[i])
			{
				xEqual = false;
				break;
			}
		}

		boolean yEqual = true;
		for (int i = 0; i < yvs.length; i++)
		{
			if (model.vertexPositionsY[i] != yvs[i])
			{
				yEqual = false;
				break;
			}
		}

		boolean zEqual = true;
		for (int i = 0; i < zvs.length; i++)
		{
			if (model.vertexPositionsZ[i] != zvs[i])
			{
				zEqual = false;
				break;
			}
		}

		return xEqual && yEqual && zEqual;
	}

	private static boolean validateRs2Indices(ModelDefinition model, ArrayList<Integer> types, ArrayList<Integer> indices)
	{
		int faceCount = model.faceCount;
		int[] _indices1 = new int[faceCount];
		int[] _indices2 = new int[faceCount];
		int[] _indices3 = new int[faceCount];

		Queue<Integer> typ = new LinkedList<>(types);
		Queue<Integer> ind = new LinkedList<>(indices);

		int index1 = 0;
		int index2 = 0;
		int index3 = 0;
		int lastIndex3 = 0;
		for (int i = 0; i < faceCount; ++i)
		{
			int pre1 = index1;
			int pre2 = index2;
			int pre3 = index3;
			int prelast3 = lastIndex3;

			int take1 = -1;
			int take2 = -1;
			int take3 = -1;

			int vertexIndexType = typ.poll();
			if (vertexIndexType == 1)
			{
				take1 = ind.poll();
				take2 = ind.poll();
				take3 = ind.poll();
				index1 = take1 + lastIndex3;
				index2 = take2 + index1;
				index3 = take3 + index2;
				lastIndex3 = index3;
				_indices1[i] = index1;
				_indices2[i] = index2;
				_indices3[i] = index3;
			}

			if (vertexIndexType == 2)
			{
				take3 = ind.poll();
				index2 = index3;
				index3 = take3 + lastIndex3;
				lastIndex3 = index3;
				_indices1[i] = index1;
				_indices2[i] = index2;
				_indices3[i] = index3;
			}

			if (vertexIndexType == 3)
			{
				take3 = ind.poll();
				index1 = index3;
				index3 = take3 + lastIndex3;
				lastIndex3 = index3;
				_indices1[i] = index1;
				_indices2[i] = index2;
				_indices3[i] = index3;
			}

			if (vertexIndexType == 4)
			{
				take3 = ind.poll();
				int temp = index1;
				index1 = index2;
				index2 = temp;
				index3 = take3 + lastIndex3;
				lastIndex3 = index3;
				_indices1[i] = index1;
				_indices2[i] = temp;
				_indices3[i] = index3;
			}

			boolean bool1 = model.faceVertexIndices1[i] == _indices1[i];
			boolean bool2 = model.faceVertexIndices2[i] == _indices2[i];
			boolean bool3 = model.faceVertexIndices3[i] == _indices3[i];
			if (!bool1 || !bool2 || !bool3)
			{
				int x = pre1 | pre2 | pre3 | prelast3;
			}
		}

		boolean indices1equal = true;
		for (int i = 0; i < _indices1.length; i++)
		{
			if (model.faceVertexIndices1[i] != _indices1[i])
			{
				indices1equal = false;
				break;
			}
		}

		boolean indices2equal = true;
		for (int i = 0; i < _indices2.length; i++)
		{
			if (model.faceVertexIndices2[i] != _indices2[i])
			{
				indices2equal = false;
				break;
			}
		}

		boolean indices3equal = true;
		for (int i = 0; i < _indices3.length; i++)
		{
			if (model.faceVertexIndices3[i] != _indices3[i])
			{
				indices3equal = false;
				break;
			}
		}

		return indices1equal && indices2equal && indices3equal;
	}

	public static void createRs2Indices(ModelDefinition model, ArrayList<Integer> types, ArrayList<Integer> indices)
	{
		int lastIndex1 = model.faceVertexIndices1[0];
		int lastIndex2 = model.faceVertexIndices2[0];
		int lastIndex3 = model.faceVertexIndices3[0];
		indices.add(lastIndex1);
		indices.add(lastIndex2 - lastIndex1);
		indices.add(lastIndex3 - lastIndex2);
		types.add(1);

		for (int i = 1; i < model.faceCount; i++)
		{
			int index1 = model.faceVertexIndices1[i];
			int index2 = model.faceVertexIndices2[i];
			int index3 = model.faceVertexIndices3[i];
			int indexType;

			if (index2 == lastIndex3 && index1 == lastIndex1)
			{
				indexType = 2;
				indices.add(index3 - lastIndex3);
			}
			else if (index2 == lastIndex2 && index1 == lastIndex3)
			{
				indexType = 3;
				indices.add(index3 - lastIndex3);
			}
			else if (index2 == lastIndex1 && index1 == lastIndex2)
			{
				indexType = 4;
				indices.add(index3 - lastIndex3);
			}
			else
			{
				indexType = 1;
				indices.add(index1 - lastIndex3);
				indices.add(index2 - index1);
				indices.add(index3 - index2);
			}

			types.add(indexType);

			lastIndex1 = index1;
			lastIndex2 = index2;
			lastIndex3 = index3;
		}
	}

	private static void writeShortSmart(DataOutputStream stream, int value) throws IOException
	{
		int unsigned = (value + 64) & 0xffff;
		if (unsigned < 128)
		{
			stream.writeByte(value + 64);
		}
		else
		{
			stream.writeShort(value + 0xc000);
		}
	}
}
