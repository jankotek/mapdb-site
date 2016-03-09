import org.junit.Test
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.ChartBuilder_Category
import org.knowm.xchart.internal.style.Styler
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * Generate performance charts for website. Results are taken from 'benchmark.properties' and converted to bitmap images
 */
class BenchmarkCharts{

    val DIR = "target/charts/"

    init{
        File(DIR).mkdirs()
    }

    val properties = {
        val p = Properties()
        p.load(FileInputStream("benchmark.properties"))
        HashMap<String,String>(p as Map<String,String>)
    }()

    @Test fun maps(){
        val keys=listOf(
                "BTreeMap",
                "HTreeMap",
                "BTreeMap_heap",
                "HTreeMap_heap",
                "ConcurrentHashMap",
                "ConcurrentSkipListMap"
        )

        for(op in listOf("get","insert","update")){
            val chart = ChartBuilder_Category()
                .width(800).height(400)
//                .title("Maps - $op")
                //.xAxisTitle("Color")
                .yAxisTitle("Time")
                .theme(Styler.ChartTheme.Matlab)
                .build()

            val oo = listOf(op)
            for(key in keys) {
                chart.addSeries(key, oo ,
                    oo.map { properties[key+"_"+it]!!.toInt() }
                )
            }

            BitmapEncoder.saveBitmap(chart, DIR+"maps_$op.png", BitmapEncoder.BitmapFormat.PNG);
        }
    }

    @Test fun maps_memory_usage(){
        val keys=listOf(
            "MapMemoryUsage#BTreeMap",
            "MapMemoryUsage#BTreeMap_heap",
            "MapMemoryUsage#BTreeMap_pump",
            "MapMemoryUsage#HTreeMap",
            "MapMemoryUsage#HTreeMap_heap",
            "MapMemoryUsage#SortedTableMap",
            "MapMemoryUsage#ConcurrentHashMap",
            "MapMemoryUsage#ConcurrentSkipListMap"
        )

        val chart = ChartBuilder_Category()
                .width(800).height(400)
//                .title("Maps - number of elements in 5GB")
                .yAxisTitle("Count")
                .theme(Styler.ChartTheme.Matlab)
                .build()

        for(k in keys){
            chart.addSeries(k.split("#").last(), listOf("map"), listOf(properties[k]!!.toInt()))
        }
        BitmapEncoder.saveBitmap(chart, DIR+"maps_memory_usage.png", BitmapEncoder.BitmapFormat.PNG);

    }

}