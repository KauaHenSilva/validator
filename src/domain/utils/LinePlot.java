package domain.utils;

import domain.utils.TTest;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class LinePlot {

    public static void main(String[] args) {
        // Exemplo de dados
//        List<? extends Number> xData = Arrays.asList(1, 2, 3, 4, 5);
//        List<Double> modelData = Arrays.asList(0.5, 0.7, 0.9, 1.1, 1.3);
//        List<Double> modelError = Arrays.asList(0.1, 0.1, 0.1, 0.1, 0.1);
//        List<Double> experimentData = Arrays.asList(0.6, 0.8, 1.0, 1.2, 1.4);
//        List<Double> experimentError = Arrays.asList(0.2, 0.2, 0.2, 0.2, 0.2);

//        List<? extends Number> xData = Arrays.asList(2.0E-4, 0.001);
        List<? extends Number> xData = Arrays.asList(1, 2);
        List<Double> modelData = Arrays.asList(1000.0,2000.0);
        List<Double> modelError = Arrays.asList(1.5,1.5);
        List<Double> experimentData = Arrays.asList(2004.0, 2986.0);
        List<Double> experimentError = Arrays.asList(182.1818181818182, 271.45454545454544);

        try {
            generateGraph(true, "Configuration", "MRT", "ms", "Model", "Experiment", String.valueOf(System.currentTimeMillis()), "./", "graphs", xData, modelData, modelError, experimentData, experimentError);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateGraph(boolean hasICBars, String xPrintedName, String yPrintedName, String yPrintedUnit, String modelSeriesName, String experimentSeriesName, String graphFileName, String xmlPath, String graphDirName, List<? extends Number> xData, List<Double> modelData, List<Double> modelError, List<Double> experimentData, List<Double> experimentError) throws IOException {
        Locale.setDefault(Locale.US); // Define a codifica��o dos caracteres para os EUA
        Styler.LegendPosition position = Styler.LegendPosition.OutsideE;

        String yName = yPrintedName + " (" + yPrintedUnit + ")";

        // Cria o gr�fico
        XYChart chart = new XYChartBuilder().width(800).height(600).title(LinePlot.class.getSimpleName())
                .theme(Styler.ChartTheme.Matlab)
                .title("")
                .xAxisTitle(xPrintedName)
                .yAxisTitle(yName)
                .build();

        // Adiciona as s�ries de dados
        if (hasICBars) {
            chart.addSeries(modelSeriesName, xData, modelData, modelError);
            chart.addSeries(experimentSeriesName, xData, experimentData, experimentError);
        } else {
            chart.addSeries(modelSeriesName, xData, modelData);
            chart.addSeries(experimentSeriesName, xData, experimentData);
        }

        // Personaliza o gr�fico
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        chart.getStyler().setPlotGridLinesVisible(false);
        DecimalFormat xFormat = new DecimalFormat("#.##########", symbols);
        chart.getStyler().setXAxisDecimalPattern(xFormat.toLocalizedPattern());
        chart.getStyler().setAxisTickLabelsFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        chart.getStyler().setAxisTitleFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        chart.getStyler().setLegendFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        chart.getStyler().setLegendPosition(position);
        chart.getStyler().setErrorBarsColorSeriesColor(true);
        chart.getStyler().setYAxisDecimalPattern("0.00000");

        boolean xHasBigScale = xData.stream().anyMatch(d -> d.doubleValue() > 1.0);
        boolean yHasBigScale = Stream.concat(modelData.stream(), experimentData.stream()).anyMatch(d -> d > 0.01);

        if (xHasBigScale) {
            xFormat = new DecimalFormat("#.##", symbols);
            chart.getStyler().setXAxisDecimalPattern(xFormat.toLocalizedPattern());
        }

        if (yHasBigScale) {
            DecimalFormat yFormat = new DecimalFormat("#.##", symbols);
            chart.getStyler().setYAxisDecimalPattern(yFormat.toLocalizedPattern());
        }

        // Define o caminho para salvar o gr�fico
        String graphPath = xmlPath + (xmlPath.endsWith(File.separator) ? "" : File.separator) + graphDirName;

        // Verifica e cria o diret�rio, se necess�rio
        File directory = new File(graphPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Salva o gr�fico
        BitmapEncoder.saveBitmapWithDPI(chart, graphPath + "/" + graphFileName, BitmapEncoder.BitmapFormat.PNG, 400);

        System.out.println("O gr�fico foi gerado com sucesso no diret�rio:\n" + graphPath);

        // Calcular teste t para cada ponto
        List<Double> tValues = calculateTValues(modelData, experimentData, modelError, experimentError);
        List<Double> pValues = calculatePValues(tValues, xData.size()); // df = n1 + n2 - 2
        List<Boolean> significance = determineSignificance(pValues);

        printLatexTable(xPrintedName, xData, modelData, modelError, experimentData, experimentError, tValues, pValues, significance);


        // Tenta abrir o gr�fico ap�s salvar
        try {
            File file = new File(graphPath + "/" + graphFileName + ".png");
            if (file.exists()) {
                Desktop.getDesktop().open(file); // Abre o arquivo de imagem gerado
            } else {
                System.out.println("Arquivo n�o encontrado: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Momento de Fim: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }


    // Função para calcular os valores de t para cada ponto do gráfico
    private static List<Double> calculateTValues(List<Double> modelData, List<Double> experimentData, List<Double> modelError, List<Double> experimentError) {
        List<Double> tValues = new ArrayList<>();
        for (int i = 0; i < modelData.size(); i++) {
            double modelMean = modelData.get(i);
            double modelStdDev = modelError.get(i);
            double experimentMean = experimentData.get(i);
            double experimentStdDev = experimentError.get(i);
            int n = 20; // Tamanho da amostra, ajuste conforme necessário

            double tValue = TTest.calculateTValue(modelMean, experimentMean, modelStdDev, experimentStdDev, n, n);
            tValues.add(tValue);
        }
        return tValues;
    }

    // Função para calcular os valores de p para cada ponto do gráfico
    private static List<Double> calculatePValues(List<Double> tValues, int df) {
        List<Double> pValues = new ArrayList<>();
        for (double tValue : tValues) {
            double pValue = TTest.calculatePValue(tValue, df);
            pValues.add(pValue);
        }
        return pValues;
    }

    // Função para determinar a significância estatística com base nos valores de p
    private static List<Boolean> determineSignificance(List<Double> pValues) {
        List<Boolean> significance = new ArrayList<>();
        for (double pValue : pValues) {
            significance.add(TTest.isSignificant(pValue));
        }
        return significance;
    }

    // Função para imprimir a tabela LaTeX com os resultados do teste t
    // Função para imprimir a tabela LaTeX com os resultados do teste t
    private static void printLatexTable(String xPrintedName, List<? extends Number> xData, List<Double> modelData, List<Double> modelError, List<Double> experimentData, List<Double> experimentError, List<Double> tValues, List<Double> pValues, List<Boolean> significance) {
        System.out.println("\\begin{table}[ht]");
        System.out.println("\\centering");
        System.out.println("\\begin{tabular}{|c|c|c|c|c|c|c|}");
        System.out.println("\\hline");
        System.out.println(xPrintedName +" & Model Mean & Model SD & Experiment Mean & Experiment SD & t Value & p Value \\\\");

        System.out.println("\\hline");

        DecimalFormat df = new DecimalFormat("0.00"); // Formato para dois dígitos decimais

        for (int i = 0; i < xData.size(); i++) {
            Number ponto = xData.get(i);
            double mediaModelo = modelData.get(i);
            double sdModelo = modelError.get(i);
            double mediaExperimento = experimentData.get(i);
            double sdExperimento = experimentError.get(i);
            double valorT = tValues.get(i);
            double valorP = pValues.get(i);
            String significativo = significance.get(i) ? "*" : ""; // Indicador de significância

            System.out.println(ponto + " & " +
                    df.format(mediaModelo) + " & " +
                    df.format(sdModelo) + " & " +
                    df.format(mediaExperimento) + " & " +
                    df.format(sdExperimento) + " & " +
                    df.format(valorT) + " & " +
                    df.format(valorP) + significativo + " \\\\");
        }

        System.out.println("\\hline");
        System.out.println("\\end{tabular}");
        System.out.println("\\caption{T Test Result.}");
        System.out.println("\\label{tab:test-t-results}");
        System.out.println("\\end{table}");
    }


}
