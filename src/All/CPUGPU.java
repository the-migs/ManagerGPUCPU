package All;
import com.sun.management.OperatingSystemMXBean;
import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;

public class CPUGPU {

    // metodo para pegar o tipo da GPU
    public static String getTypeGPU(){
        // cria o comando para descobrir o tipo da GPU
        try {
            String typeGPU = (new oshi.SystemInfo().getHardware().getGraphicsCards().get(0).getName()).toLowerCase();

            if(typeGPU.contains("amd") || typeGPU.contains("radeon") || typeGPU.contains("advanced micro devices")){ typeGPU = "amd"; }
            else if(typeGPU.contains("intel")){ typeGPU = "intel"; }
            else if(typeGPU.contains("nvidia") || typeGPU.contains("geforce") || typeGPU.contains("rtx") || typeGPU.contains("gtx")){ typeGPU = "nvidia"; }


            return typeGPU;
        } catch (Exception e) {
            e.printStackTrace();
            return "error_getTypeGPU";
        }
    }
    // metodo para pegar a % de uso da GPU
    public static String getValueGPU(String comand, String parameterOne, String parameterTwo) {
        Process p = null;
        // comando que vai pedir para o sistema a % de uso
        ProcessBuilder pb = new ProcessBuilder(
                comand,
                parameterOne,
                parameterTwo);
        try {
            p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String valueGPU = (reader.readLine());
            return (valueGPU != null) ? valueGPU + "%" : "0%";
        }
        catch (Exception e) {
            e.printStackTrace();
            return "error_getValueGPU";
        }
        finally {
            if(p != null) {
                p.destroyForcibly();
            }
        }
    }
    public static String getCPU() {
        // codigo sem resumir:
        // com.sun.management.OperatingSystemMXBean bean =
        // (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        // vai fazer um cast do objeto limitado para um tipo avancado que contenha os valores da CPU
        // tudo sem riscos
        OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        String valueCPU = (String.format("%.0f", bean.getSystemCpuLoad() * 100)) + "%";

        return valueCPU;
    }
    public static String getGPU() {
        // vai pegar o nome do sistema operacional
        String OS = System.getProperty("os.name").toLowerCase();
        String typeGPU = getTypeGPU();
        if (OS.contains("lin")) {
            switch (typeGPU) {
                case "nvidia" -> {
                    return getValueGPU("nvidia-smi","--query-gpu=utilization.gpu","--format=csv,noheader,nounits");
                }
                case "amd" -> {

                    return getValueGPU("bash","-c","radeontop -b -d - | sed -n 's/.*gpu \\([0-9.]*\\)%.*/\\1/p'");
                }
                case "intel" -> {
                    return getValueGPU("bash","-c","intel_gpu_top -J -s 1000 | jq '.engines.RenderBusy' | head -n 1");
                }
                default -> {return "type_incompatible";}
            }
        }
        if (OS.contains("win")) {
            switch (typeGPU) {
                case "nvidia" -> {
                    String testNvidia = getValueGPU("nvidia-smi", "--query-gpu=utilization.gpu", "--format=csv,noheader,nounits");
                    if (testNvidia.contains("error")) {
                        return getValueGPU(
                                "powershell.exe",
                                "-Command",
                                "(Get-Counter '\\GPU Engine(*)\\Utilization Percentage').CounterSamples | " +
                                        "Where-Object { $_.InstanceName -match 'engtype_3D' } | " +
                                        "ForEach-Object { [math]::Round($_.CookedValue) } | " +
                                        "Sort-Object -Descending | " +
                                        "Select-Object -First 1"
                        );
                    }
                    return testNvidia;
                }
                case "amd", "intel" -> {
                    return getValueGPU(
                            "powershell.exe",
                            "-Command",
                            "(Get-Counter '\\GPU Engine(*)\\Utilization Percentage').CounterSamples | " +
                                    "Where-Object { $_.InstanceName -match 'engtype_3D' } | " +
                                    "ForEach-Object { [math]::Round($_.CookedValue) } | " +
                                    "Sort-Object -Descending | " +
                                    "Select-Object -First 1"
                    );
                }
                default -> {
                    return "type_incompatible";
                }
            }
        }
        if (OS.contains("mac")) {return "Use a decent SO"; }

        else {return "OS_incompatible";}
    }
}