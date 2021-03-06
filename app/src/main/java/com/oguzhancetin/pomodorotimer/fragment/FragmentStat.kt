package com.oguzhancetin.pomodorotimer.fragment

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.oguzhancetin.pomodorotimer.MyApplication
import com.oguzhancetin.pomodorotimer.R
import com.oguzhancetin.pomodorotimer.di.AppContainer
import com.oguzhancetin.pomodorotimer.di.FragmentStatContainer
import com.oguzhancetin.pomodorotimer.database.Pomodoro
import com.oguzhancetin.pomodorotimer.databinding.FragmentStatBinding
import com.oguzhancetin.pomodorotimer.util.IntegerFormatter
import com.oguzhancetin.pomodorotimer.util.MyaxisFormatter
import com.oguzhancetin.pomodorotimer.viewmodel.FragmentStatusViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList


class FragmentStat : Fragment() {

    private var viewModel: FragmentStatusViewModel? = null

    private lateinit var appContainer: AppContainer
    private lateinit var mLineChart: BarChart

    var yAxisMax: Float = 10f


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        val binding = FragmentStatBinding.inflate(inflater)
        setHasOptionsMenu(true)
        initializeContainer()
        viewModel = appContainer.fragmentStatContainer?.fragmentStatusViewModel


        mLineChart = binding.chart
        configureLineChart(mLineChart)


        viewModel?.allPomodoro?.observe(viewLifecycleOwner, { pomodoros ->
            pomodoros?.let {
                setGraphData(pomodoros)
                mLineChart.invalidate()
            }
        })
        return binding.root
    }

    private fun initializeContainer() {
        appContainer = (requireActivity().application as MyApplication).appContainer
        appContainer.fragmentStatContainer =
            FragmentStatContainer(appContainer.myViewModelFactory, this)

    }

    private fun configureLineChart(mLineChart: BarChart) {
        mLineChart.apply {
            xAxis.valueFormatter = MyaxisFormatter()
            axisRight.isEnabled = false
            xAxis.setDrawGridLines(false)
            setTouchEnabled(false)
            xAxis.axisMaximum = 6.5f
            xAxis.granularity = 1f
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = yAxisMax
            xAxis.yOffset = 13f
        }

    }

    //set graph entries
    @RequiresApi(Build.VERSION_CODES.O)
    fun setGraphData(sortedPomodoros: List<Pomodoro>) {
        val days = arrayOf(0, 0, 0, 0, 0, 0, 0)

        //sorted to obtain which day and added to days array
        sortedPomodoros.forEach {

            val pday = LocalDateTime.ofInstant(
                Date(it.finished_date_milis).toInstant(),
                ZoneId.systemDefault()
            )

            when (pday.dayOfWeek.value.toString()) {
                "1" -> days.set(0, days.get(0) + 1)
                "2" -> days.set(1, days.get(1) + 1)
                "3" -> days.set(2, days.get(2) + 1)
                "4" -> days.set(3, days.get(3) + 1)
                "5" -> days.set(4, days.get(4) + 1)
                "6" -> days.set(5, days.get(5) + 1)
                "7" -> days.set(6, days.get(6) + 1)
            }
        }
        // }
        if (days.sortedArrayDescending().first() > mLineChart.axisLeft.axisMaximum) {
            mLineChart.axisLeft.axisMaximum = (days.sortedArrayDescending().first() + 2) * 1f
        }

        val entries = ArrayList<BarEntry>()
        for (x in 0 until days.size) {
            entries.add(BarEntry(x.toFloat(), days.get(x).toFloat()))
        }

        val dataSet = BarDataSet(entries, "Weekly Distribution").also {
            it.valueFormatter = IntegerFormatter()
            val color = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            it.setColor(color)


        }

        BarData(dataSet).also {
            it.barWidth = 0.6f
            mLineChart.data = it
            val desc = Description()
            desc.text = ""
            mLineChart.description = desc
            mLineChart.invalidate()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_alldata -> {
                viewModel?.deleteAlldata()
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appContainer.fragmentStatContainer = null
    }


}