package com.example.dall_e.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.dall_e.databinding.FragmentHomeBinding
import com.example.dall_e.update_activity.UpdateActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.let { binding ->
            binding.update.setOnClickListener {
                val intent = Intent(context, UpdateActivity::class.java)
                startActivity(intent)
            }
        }
        _binding?.let { binding ->
            binding.sync.setOnClickListener {
                val intent = Intent(context, SyncActivity::class.java)
                startActivity(intent)
            }
        }
        _binding?.let { binding ->
            binding.backup.setOnClickListener {
                val intent = Intent(context, SyncActivity::class.java)
                intent.putExtra("logout",true)
                startActivity(intent)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}