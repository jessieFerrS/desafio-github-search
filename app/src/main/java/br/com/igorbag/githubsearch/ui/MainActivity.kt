package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    private lateinit var nomeUsuario: EditText
    private lateinit var btn_pesquisar: Button
    private lateinit var listaRepositories: RecyclerView
    private lateinit var githubApi: GitHubService
    private lateinit var barraCarregamento : ProgressBar
    private lateinit var imagemWifiOff : ImageView
    private lateinit var semWifi : TextView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayShowHomeEnabled(true)

        setContentView(R.layout.activity_main)
        setupView()
        showUserName()
        setupRetrofit()
        setupListeners()

    }
    // Inicializa as visualizações
    private fun setupView() {
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btn_pesquisar = findViewById(R.id.btn_pesquisar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
        barraCarregamento = findViewById(R.id.pb_carregamento)
        imagemWifiOff = findViewById(R.id.iv_wifi_off)
        semWifi = findViewById(R.id.tv_wifi_off)
    }

    // Configura o Retrofit para fazer chamadas à API do GitHub
    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    // Configura os ouvintes de eventos
    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupListeners() {
        btn_pesquisar.setOnClickListener {
            val conexao = isInternetAvailable()

            if (!conexao) {
                // Exibe uma mensagem de erro quando não há conectividade
                imagemWifiOff.isVisible = true
                semWifi.isVisible = true
            } else {
                imagemWifiOff.isVisible = false
                semWifi.isVisible = false

                val nomePesquisar = nomeUsuario.text.toString()
                getAllReposByUserName(nomePesquisar)
                saveUserLocal()
                listaRepositories.isVisible = false
            }
        }
    }

    // Verifica a disponibilidade de conexão à Internet
    @RequiresApi(Build.VERSION_CODES.M)
    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

        return actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    // Faz a solicitação à API do GitHub para obter repositórios por nome de usuário
    private fun getAllReposByUserName(userName: String) {
        if (userName.isNotEmpty()) {
            barraCarregamento.isVisible = true

            githubApi.getAllRepositoriesByUser(userName)
                .enqueue(object : Callback<List<Repository>> {
                    override fun onResponse(
                        call: Call<List<Repository>>,
                        response: Response<List<Repository>>
                    ) {
                        if (response.isSuccessful) {
                            barraCarregamento.isVisible = false
                            listaRepositories.isVisible = true

                            val repositories = response.body()

                            repositories?.let {
                                setupAdapter(repositories)
                            }

                        } else {
                            barraCarregamento.isVisible = false
                            val context = applicationContext
                            Toast.makeText(context, R.string.response_error, Toast.LENGTH_LONG)
                                .show()
                        }
                    }

                    override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                        barraCarregamento.isVisible = false
                        val context = applicationContext
                        Toast.makeText(context, R.string.response_error, Toast.LENGTH_LONG).show()
                    }

                })
        }
    }

    // Configura o adaptador do RecyclerView
    private fun setupAdapter(list: List<Repository>) {
        val adapter = RepositoryAdapter(this, list)
        listaRepositories.adapter = adapter
    }

    // Salva o nome de usuário localmente
    private fun saveUserLocal() {
        val usuarioInformado = nomeUsuario.text.toString()
        val sharedPreference = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPreference.edit()) {
            putString("saved_username", usuarioInformado)
            apply()
        }
    }

    // Exibe o nome de usuário previamente salvo
    private fun showUserName() {
        val sharedPreference = getPreferences(MODE_PRIVATE) ?: return
        val ultimoPesquisado = sharedPreference.getString("saved_username", null)

        if (!ultimoPesquisado.isNullOrEmpty()) {
            nomeUsuario.setText(ultimoPesquisado)
        }
    }
}
