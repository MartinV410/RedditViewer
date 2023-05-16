package com.example.redditviewer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.redditviewer.GenericUIState
import com.example.redditviewer.databinding.FragmentLoginBinding
import com.example.redditviewer.view.LoginViewModel


class LoginFragment: Fragment() {

    private val viewModel: LoginViewModel by viewModels{ LoginViewModel.Factory }
    private val args: LoginFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        // navigate to subreddit fragment if user is already logged in
        if(!viewModel.needsLogin() && !args.needsRevoke) {
            val direction = LoginFragmentDirections.actionFragmentLogin2ToSubredditActivity()
            findNavController().navigate(direction)
        }

        viewModel.loginUiState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when(it) {
                    is GenericUIState.Error -> {
                        Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                        binding.cardView.visibility = View.VISIBLE
                        binding.webview.visibility = View.GONE
                    }
                    GenericUIState.Loading -> {}
                    GenericUIState.Success -> {
//                        binding.cardView.visibility = View.VISIBLE
//                        binding.webview.visibility = View.GONE
                        val direction = LoginFragmentDirections.actionFragmentLogin2ToSubredditActivity()
                        findNavController().navigate(direction)
                    }
                }
            }
        })

        binding.login.setOnClickListener {
            // code from https://github.com/KirkBushman/Android-Reddit-OAuth2
            val browser = binding.webview
            binding.cardView.visibility = View.GONE
            browser.visibility = View.VISIBLE
            viewModel.retrieveToken(browser, requireContext())
        }


        return binding.root
    }
}