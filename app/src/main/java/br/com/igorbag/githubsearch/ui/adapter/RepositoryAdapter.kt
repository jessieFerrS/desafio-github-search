package br.com.igorbag.githubsearch.ui.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository

class RepositoryAdapter(private val context: Context, private val repositories: List<Repository>) : RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    // Método para criar a visualização do item do RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.repository_item, parent, false)
        return ViewHolder(view)
    }

    // Método para vincular os dados aos elementos da visualização do item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val repository = repositories[position]

        // Define um ouvinte de clique para o card do repositório para abrir o navegador com a URL do repositório
        holder.cardRepositorio.setOnClickListener {
            openBrowser(context, repository.htmlUrl)
        }

        // Define o nome do repositório
        holder.nomeRepositorio.text = repository.name

        // Define um ouvinte de clique para o ícone de compartilhamento para compartilhar o link do repositório
        holder.shareIconButton.setOnClickListener {
            shareRepositoryLink(context, repository.htmlUrl)
        }
    }

    // Retorna o número de itens na lista
    override fun getItemCount(): Int = repositories.size

    // Classe ViewHolder para manter as referências aos elementos da visualização do item
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardRepositorio : CardView
        val nomeRepositorio : TextView
        val shareIconButton : ImageView

        init {
            view.apply {
                cardRepositorio = findViewById(R.id.cv_repository)
                nomeRepositorio = findViewById(R.id.tv_repository_name)
                shareIconButton = findViewById(R.id.iv_share)
            }
        }
    }

    // Método para compartilhar o link do repositório
    private fun shareRepositoryLink(context: Context, urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }


    // Método para abrir o navegador com a URL do repositório
    private fun openBrowser(context: Context, urlRepository: String) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )
    }
}
