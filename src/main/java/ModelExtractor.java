import com.displee.cache.CacheLibrary;
import com.google.common.hash.Hashing;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import net.runelite.cache.IndexType;
import net.runelite.cache.definitions.ModelDefinition;
import net.runelite.cache.definitions.assemblers.ModelAssembler;
import net.runelite.cache.definitions.loaders.ModelLoader;
import net.runelite.cache.models.MetaExporter;
import net.runelite.cache.models.ObjExporter;
import net.runelite.cache.models.ObjImporter;
import org.antlr.v4.runtime.misc.Pair;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

@Slf4j
public class ModelExtractor
{
	static final String path = "C:/Users/X/jagexcache/oldschool/LIVE";
	static final String outputPath = "models/slayerhelmets";

	static
	{
		var file = new File(outputPath);
		file.mkdir();
	}

	public static void main(String[] args) throws IOException
	{
//		{
//			int modelID = 31553;
//			try (FileInputStream fis = new FileInputStream(new File(new File(outputPath, String.valueOf(modelID)), modelID + ".model")))
//			{
//				byte[] data = fis.readAllBytes();
//				exportBinary(data, modelID);
//			}
//		}
		var library = prepCache();
		int[] modelIds = new int[]
				{
					42673,
					42685,
					32992,
					32993,
					42676,
					42684,
					31549,
					31552,
					42680,
					42690,
					34626,
					34638,
					42674,
					42689,
					31550,
					31551,
					42681,
					42682,
					31548,
					31553,
//					33144,
//					33111,
//					37215
				};
		for (int modelId : modelIds)
		{
			byte[] data = getModelBytes(library, modelId);
			if (data == null)
			{
				continue;
			}

			exportBinary(data, modelId);
			boolean hashEqual = validateBinary(data, modelId);

//			ModelLoader modelLoader = new ModelLoader();
//			var def = modelLoader.load(modelId, data);
//
//			exportModel(def, modelId);

			log.info("Hash correct: {}", hashEqual);
		}
//		{
//			File obj = new File("blender models/no_anime_workspace.obj");
//			File mtl = new File("blender models/no_anime_workspace.mtl");
//			File meta = new File("blender models/no_anime_workspace.meta");
////		File obj = new File("blender models/female_arma_helm_model.obj");
////		File mtl = new File("blender models/female_arma_helm_model.mtl");
////		File meta = new File("blender models/female_arma_helm_model.meta");
//			var model = ObjImporter.fromObj(obj, mtl, meta);
//
//			if (model != null)
//			{
//				byte[] dataM2 = ModelAssembler.AssembleModel(model);
//				var parent = new File(outputPath);
//				parent.mkdir();
//				var dir = new File(parent, "converted/");
//				dir.mkdir();
//				exportBinary(dataM2, 14398, dir);
//				exportModel(model, 14398, dir);
//			}
//		}
//		{
//			var parent = new File(outputPath);
//			String name = String.valueOf(14398);
//			var dir = new File(parent, "converted/"+name+"/");
//			File obj = new File(dir, name+".obj");
//			File mtl = new File(dir, name+".mtl");
//			File meta = new File(dir, name+".meta");
//			var model = ObjImporter.fromObj(obj, mtl, meta);
//			byte[] dataM2 = ModelAssembler.AssembleModel(model);
//
//			ModelLoader modelLoader = new ModelLoader();
//			var def = modelLoader.load(998, dataM2);
//			exportModel(def, 998);
//		}
	}

	private static boolean testShort(ModelDefinition model)
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
		ArrayList<Pair<Integer, Integer>> output = new ArrayList<>();

		ArrayList<Integer> indexTypes = new ArrayList<>();
		ArrayList<Integer> indices = new ArrayList<>();

		ArrayList<Integer> vFlags = new ArrayList<>();
		ArrayList<Integer> xVertices = new ArrayList<>();
		ArrayList<Integer> yVertices = new ArrayList<>();
		ArrayList<Integer> zVertices = new ArrayList<>();
		try
		{
			ModelAssembler.createRs2Vertices(model, vFlags, xVertices, yVertices, zVertices);

			Queue<Integer> xQueue = new LinkedList<>(xVertices);
			Queue<Integer> yQueue = new LinkedList<>(yVertices);
			Queue<Integer> zQueue = new LinkedList<>(zVertices);
			for (Integer vFlag : vFlags)
			{
				if ((vFlag & 1) != 0)
				{
					int vertexXOffset = xQueue.poll();
					output.add(new Pair<>(vertexXOffset, 1));
					writeShortSmart(dataOutputStream, vertexXOffset);
				}

				if ((vFlag & 2) != 0)
				{
					int vertexYOffset = yQueue.poll();
					output.add(new Pair<>(vertexYOffset, 1));
					writeShortSmart(dataOutputStream, vertexYOffset);
				}

				if ((vFlag & 4) != 0)
				{
					int vertexZOffset = zQueue.poll();
					output.add(new Pair<>(vertexZOffset, 1));
					writeShortSmart(dataOutputStream, vertexZOffset);
				}
			}

			ModelAssembler.createRs2Indices(model, indexTypes, indices);
			Queue<Integer> indexQueue = new LinkedList<>(indices);
			for (Integer indexType : indexTypes)
			{
				if (indexType == 2 || indexType == 3 || indexType == 4)
				{
					int index3 = indexQueue.poll();
					output.add(new Pair<>(index3, 1));
					writeShortSmart(dataOutputStream, index3);
				}
				else
				{
					int index1 = indexQueue.poll();
					int index2 = indexQueue.poll();
					int index3 = indexQueue.poll();
					writeShortSmart(dataOutputStream, index1);
					output.add(new Pair<>(index1, 1));
					writeShortSmart(dataOutputStream, index2);
					output.add(new Pair<>(index2, 1));
					writeShortSmart(dataOutputStream, index3);
					output.add(new Pair<>(index3, 1));
				}
			}

			for (short faceColor : model.faceColors)
			{
				dataOutputStream.writeShort(faceColor);
				output.add(new Pair<>((int) faceColor, 2));
			}
			dataOutputStream.close();
		}
		catch (IOException e)
		{
			log.warn("Couldnt write to output", e);
		}

		net.runelite.cache.io.InputStream inputStream = new net.runelite.cache.io.InputStream(outputStream.toByteArray());

		for (int i = 0; i < output.size(); i++)
		{
			int original = output.get(i).a;
			int type = output.get(i).b;
			int toCompare;
			if (type == 1)
			{
				toCompare = inputStream.readShortSmart();
			}
			else
			{
				toCompare = (short) inputStream.readUnsignedShort();
			}

			if (original != toCompare)
			{
				return false;
			}
		}
		return true;
	}

	public static void writeShortSmart(DataOutputStream stream, int value) throws IOException
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

	private static ModelDefinition importModel(int modelId)
	{
		File dir = new File(outputPath, String.valueOf(modelId));
		return importModel(dir, modelId);
	}

	private static ModelDefinition importModel(File dir, int modelId)
	{
		String objFile = modelId + ".obj";
		String mtlFile = modelId + ".mtl";
		String metaFile = modelId + ".meta";
		return ObjImporter.fromObj(new File(dir, objFile), new File(dir, mtlFile), new File(dir, metaFile));
	}

	private static boolean compareModels(ModelDefinition model1, ModelDefinition model2)
	{
		boolean vertexCountSame = model1.vertexCount == model2.vertexCount;
		boolean faceCountSame = model1.faceCount == model2.faceCount;

		if (!vertexCountSame || !faceCountSame)
		{
			log.info("Mismatch in faceCount or vertexCount");
			return false;
		}

		int faceCount = model1.faceCount;
		int vertexCount = model1.vertexCount;

		HashMap<Integer, Integer> vertexIndices1Model1 = new HashMap<>();
		HashMap<Integer, Integer> vertexIndices2Model1 = new HashMap<>();
		HashMap<Integer, Integer> vertexIndices3Model1 = new HashMap<>();
		HashMap<Integer, Integer> vertexIndices1Model2 = new HashMap<>();
		HashMap<Integer, Integer> vertexIndices2Model2 = new HashMap<>();
		HashMap<Integer, Integer> vertexIndices3Model2 = new HashMap<>();
		HashMap<Short, Integer> faceColorsModel1 = new HashMap<>();
		HashMap<Short, Integer> faceColorsModel2 = new HashMap<>();

		for (int i = 0; i < faceCount; i++)
		{
			vertexIndices1Model1.computeIfPresent(model1.faceVertexIndices1[i], (k, v) -> v + 1);
			vertexIndices1Model1.putIfAbsent(model1.faceVertexIndices1[i], 1);
			vertexIndices2Model1.computeIfPresent(model1.faceVertexIndices2[i], (k, v) -> v + 1);
			vertexIndices2Model1.putIfAbsent(model1.faceVertexIndices2[i], 1);
			vertexIndices3Model1.computeIfPresent(model1.faceVertexIndices3[i], (k, v) -> v + 1);
			vertexIndices3Model1.putIfAbsent(model1.faceVertexIndices3[i], 1);

			vertexIndices1Model2.computeIfPresent(model2.faceVertexIndices1[i], (k, v) -> v + 1);
			vertexIndices1Model2.putIfAbsent(model2.faceVertexIndices1[i], 1);
			vertexIndices2Model2.computeIfPresent(model2.faceVertexIndices2[i], (k, v) -> v + 1);
			vertexIndices2Model2.putIfAbsent(model2.faceVertexIndices2[i], 1);
			vertexIndices3Model2.computeIfPresent(model2.faceVertexIndices3[i], (k, v) -> v + 1);
			vertexIndices3Model2.putIfAbsent(model2.faceVertexIndices3[i], 1);

			faceColorsModel1.computeIfPresent(model1.faceColors[i], (k, v) -> v + 1);
			faceColorsModel1.putIfAbsent(model1.faceColors[i], 1);

			faceColorsModel2.computeIfPresent(model2.faceColors[i], (k, v) -> v + 1);
			faceColorsModel2.putIfAbsent(model2.faceColors[i], 1);
		}

		boolean vertexIndices1ModelCondition = compareMaps(vertexIndices1Model1, vertexIndices1Model2);
		boolean vertexIndices2ModelCondition = compareMaps(vertexIndices2Model1, vertexIndices2Model2);
		boolean vertexIndices3ModelCondition = compareMaps(vertexIndices3Model1, vertexIndices3Model2);

		if (!vertexIndices1ModelCondition || !vertexIndices2ModelCondition || !vertexIndices3ModelCondition)
		{
			log.info("Mismatch in vertexIndices");
			return false;
		}

//		boolean faceColors = compareMaps(faceColorsModel1, faceColorsModel2);
//		if (!faceColors)
//		{
//			log.info("Mismatch in faceColors");
//			return false;
//		}

		HashMap<Integer, Integer> vertexPositionsXModel1 = new HashMap<>();
		HashMap<Integer, Integer> vertexPositionsYModel1 = new HashMap<>();
		HashMap<Integer, Integer> vertexPositionsZModel1 = new HashMap<>();
		HashMap<Integer, Integer> vertexPositionsXModel2 = new HashMap<>();
		HashMap<Integer, Integer> vertexPositionsYModel2 = new HashMap<>();
		HashMap<Integer, Integer> vertexPositionsZModel2 = new HashMap<>();
		for (int i = 0; i < vertexCount; i++)
		{
			vertexPositionsXModel1.computeIfPresent(model1.vertexPositionsX[i], (k, v) -> v + 1);
			vertexPositionsXModel1.putIfAbsent(model1.vertexPositionsX[i], 1);
			vertexPositionsYModel1.computeIfPresent(model1.vertexPositionsY[i], (k, v) -> v + 1);
			vertexPositionsYModel1.putIfAbsent(model1.vertexPositionsY[i], 1);
			vertexPositionsZModel1.computeIfPresent(model1.vertexPositionsZ[i], (k, v) -> v + 1);
			vertexPositionsZModel1.putIfAbsent(model1.vertexPositionsZ[i], 1);

			vertexPositionsXModel2.computeIfPresent(model2.vertexPositionsX[i], (k, v) -> v + 1);
			vertexPositionsXModel2.putIfAbsent(model2.vertexPositionsX[i], 1);
			vertexPositionsYModel2.computeIfPresent(model2.vertexPositionsY[i], (k, v) -> v + 1);
			vertexPositionsYModel2.putIfAbsent(model2.vertexPositionsY[i], 1);
			vertexPositionsZModel2.computeIfPresent(model2.vertexPositionsZ[i], (k, v) -> v + 1);
			vertexPositionsZModel2.putIfAbsent(model2.vertexPositionsZ[i], 1);
		}

		boolean vertexPositionsXConditions = compareMaps(vertexPositionsXModel1, vertexPositionsXModel2);
		boolean vertexPositionsYConditions = compareMaps(vertexPositionsYModel1, vertexPositionsYModel2);
		boolean vertexPositionsZConditions = compareMaps(vertexPositionsZModel1, vertexPositionsZModel2);

		if (!vertexPositionsXConditions || !vertexPositionsYConditions || !vertexPositionsZConditions)
		{
			log.info("Mismatch in vertexPositions");
			return false;
		}

		return true;
	}

	private static boolean compareMaps(HashMap<?, ?> map1, HashMap<?, ?> map2)
	{
		boolean condition = true;
		for (var val : map1.entrySet())
		{
			condition &= map2.containsKey(val.getKey()) && map2.get(val.getKey()).equals(val.getValue());
		}

		return condition;
	}

	private static CacheLibrary prepCache()
	{
		return new CacheLibrary(path, false, (a, b) ->
		{
			System.out.println(a + " " + b);
		});
	}

	private static byte[] getModelBytes(CacheLibrary library, int modelId)
	{
		int index = IndexType.MODELS.getNumber();
		return library.data(index, modelId, 0);
	}

	private static void exportModel(ModelDefinition def, int modelId) throws IOException
	{
		var dir = new File(outputPath);
		dir.mkdir();
		exportModel(def, modelId, dir);
	}

	private static void exportModel(ModelDefinition def, int modelId, File parent) throws IOException
	{
		if (def == null)
		{
			return;
		}
		ObjExporter exporter = new ObjExporter(null, def);
		String objFileOut = modelId + ".obj";
		String mtlFileOut = modelId + ".mtl";
		String metaFileOut = modelId + ".meta";
		File dir = new File(parent, String.valueOf(modelId));
		dir.mkdir();

		log.info("Exporting model {} with vertex count {} to {}", modelId, def.vertexCount, new File(dir, objFileOut).getPath());

		try (PrintWriter objWriter = new PrintWriter(new FileWriter(new File(dir, objFileOut)));
			 PrintWriter mtlWriter = new PrintWriter(new FileWriter(new File(dir, mtlFileOut))))
		{
			exporter.export(objWriter, mtlWriter);
		}

		try (PrintWriter metaWriter = new PrintWriter(new FileWriter(new File(dir, metaFileOut))))
		{
			MetaExporter.export(def, metaWriter);
		}
	}


	private static void exportBinary(byte[] data, int modelId) throws IOException
	{
		var dir = new File(outputPath);
		dir.mkdir();
		exportBinary(data, modelId, dir);
	}

	private static void exportBinary(byte[] data, int modelId, File parent) throws IOException
	{
		if (data != null)
		{
//			File dir = new File(parent, String.valueOf(modelId));
//			dir.mkdir();
//			try (FileOutputStream filewriter = new FileOutputStream(new File(dir, String.valueOf(modelId))))
//			{
//				filewriter.write(data);
//			}
//
//			String hash = Hashing.sha256().hashBytes(data).toString().toUpperCase();
//			Files.write(hash.getBytes(StandardCharsets.UTF_8), new File(dir, modelId + ".hash"));

			try (FileOutputStream filewriter = new FileOutputStream(new File(parent, String.valueOf(modelId))))
			{
				filewriter.write(data);
			}

			String hash = Hashing.sha256().hashBytes(data).toString().toUpperCase();
			Files.write(hash.getBytes(StandardCharsets.UTF_8), new File(parent, modelId + ".hash"));
		}
	}

	private static boolean validateBinary(byte[] rsData, int modelId)
	{
		// rsData will be null if this script didn't exist at first
		if (rsData != null)
		{
			String overlayHash, originalHash;

			try (final InputStream hashIn = new FileInputStream(new File(new File(outputPath, String.valueOf(modelId)), modelId + ".hash")))
			{
				overlayHash = CharStreams.toString(new InputStreamReader(hashIn));
				originalHash = Hashing.sha256().hashBytes(rsData).toString();
			}
			catch (IOException e)
			{
				System.out.println("Couldnt open hash file");
				return false;
			}

			// Check if hash is correct first, so we don't have to load the overlay file if it doesn't match
			if (!overlayHash.equalsIgnoreCase(originalHash))
			{
				System.out.println("Hash mismatch");
				System.out.println("original " + originalHash);
				System.out.println("overlay  " + overlayHash);
				return false;
			}

			return true;
		}
		return false;
	}
}
