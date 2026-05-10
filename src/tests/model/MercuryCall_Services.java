package tests.model;

import org.knowm.xchart.style.Styler;
import org.ufpi.mercurycall.util.facade.general.GeneralEvaluationLinePlotNonAbsorbingModel;
import org.ufpi.mercurycall.util.facade.utils.EssentialParametersGroup;
import org.ufpi.mercurycall.util.facade.utils.FacadeUtil;
import org.ufpi.mercurycall.util.facade.utils.Params;
import org.ufpi.mercurycall.util.facade.utils.plotgraph.CustomizedLinePlotInformation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MercuryCall_Services {
    /**
     * @param args the command line arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        FacadeUtil.disableWarning();
        EssentialParametersGroup ep = new EssentialParametersGroup(
                0.02,
                "src/tests/model/",
                "model.xml",
                "AD",
                new Double[]{100.0},
                "C",
                new Double[]{1.0,2.0,3.0,4.0,5.0},
                "graphs_services"
        );

        CustomizedLinePlotInformation clpi = new CustomizedLinePlotInformation(
                "MRT",
                "Mean Response Time",
                "ms",
                "Number of Services",
                "ms",
                Styler.LegendPosition.InsideNE,
                new String[]{"AD = 100"},
                "MRT");

        new GeneralEvaluationLinePlotNonAbsorbingModel().solve(new Params(ep,clpi));

        CustomizedLinePlotInformation clpi_dp = new CustomizedLinePlotInformation(
                "DP",
                "Drop",
                "s",
                "Number of Services",
                "msg/ms",
                Styler.LegendPosition.InsideSE,
                new String[]{"AD = 100"},
                "DP");

        new GeneralEvaluationLinePlotNonAbsorbingModel().solve(new Params(ep, clpi_dp));

        String caminhoCSV = "src/tests/model/graphs_services/MRT.csv"; // Substitua com o caminho do seu arquivo
        lerCSV(caminhoCSV);

        System.exit(0);
    }

    public static void lerCSV(String caminhoCSV) {
        String line;
        String separator = ";";

        List<String> column1 = new ArrayList<>();
        List<String> column2 = new ArrayList<>();
        List<String> column3 = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoCSV))) {
            // Ignorar a primeira linha (cabeçalho)
            br.readLine();

            while ((line = br.readLine()) != null) {
                // Dividir as colunas pelo separador ";"
                String[] parts = line.split(separator);

                // Arredondar a coluna 1 para 4 casas decimais
                double col1Value = Double.parseDouble(parts[0]);
                column1.add(String.format("%d", (int) col1Value));

                // Dividir a segunda coluna por " / " para obter as colunas 2 e 3
                String[] secondPart = parts[1].split(" / ");

                // Arredondar a coluna 2 e 3 para 2 casas decimais
                double col2Value = Double.parseDouble(secondPart[0]);
                double col3Value = Double.parseDouble(secondPart[1]);

                column2.add(String.format("%.2f", col2Value));
                column3.add(String.format("%.2f", col3Value));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Printar os resultados
        System.out.println();
        System.out.println();
        System.out.println("variatingServices.qtdServices=" + String.join(",", column1));
        System.out.println("mrtsFromModel=" + String.join(",", column2));
        System.out.println("sdvsFromModel=" + String.join(",", column3));
    }
}
