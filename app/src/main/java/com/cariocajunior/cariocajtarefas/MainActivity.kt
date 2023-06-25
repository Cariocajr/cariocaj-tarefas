package com.cariocajunior.cariocajtarefas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    private lateinit var edName: EditText
    private lateinit var edEmail: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnView: Button
    private lateinit var btnUpdate: Button
    private lateinit var imgv_carioca: ImageView

    private var isFirstView: Boolean = false

    private lateinit var sqliteHelper: SQLiteHelper
    private lateinit var recyclerView: RecyclerView
    private var adapter: StudentAdapter? = null
    private var std: StudentModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initRecyclerView()
        sqliteHelper = SQLiteHelper(this)
        fadeInImage()

        btnAdd.setOnClickListener { addStudent() }
        btnView.setOnClickListener { getStudent() }
        btnUpdate.setOnClickListener { updateStudent() }
        //now we need to delete record


        adapter?.setOnClickItem {
            Toast.makeText(this, it.name, Toast.LENGTH_SHORT).show()
            //now we need to update record
            edName.setText(it.name)
            edEmail.setText(it.email)
            std = it
        }

        adapter?.setOnClickDeleteItem {
            deleteStudent(it.id)
        }
    }

    private fun getStudent() {
        val stdList = sqliteHelper.getAllStudent()

        if (stdList.isEmpty()) {
            Toast.makeText(this, "Adicione uma tarefa para poder visualizar", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val adjustedStdList = stdList.mapIndexed { index, student ->
            student.copy(id = index + 1)
        }

        //display data in recyclerview
        adapter?.addItems(stdList)

        fadeOutImage()
    }

    private fun addStudent() {
        val name = edName.text.toString()
        val email = edEmail.text.toString()

        if (name.isEmpty()) {
            Toast.makeText(
                this,
                "Por favor, preencha o primeiro campo corretamente!",
                Toast.LENGTH_SHORT
            )
                .show()
        } else {
            var std = StudentModel(name = name, email = email)
            val insertedId = sqliteHelper.insertStudent(std)
            //Check insert success or not success  // editei
            if (insertedId > -1) {
                std = std.copy(id = insertedId.toInt())
                Toast.makeText(this, "Tarefa adicionada com sucesso!", Toast.LENGTH_SHORT).show()
                clearEditText()
                getStudent()
            } else {
                Toast.makeText(this, "Erro ao adicionar tarefa!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStudent() {
        var name = edName.text.toString()
        val email = edEmail.text.toString()

        //checa pra ver se a tarefa gravada foi editada
        if (name == std?.name && email == std?.email) {
            Toast.makeText(this, "Altere os dados para atualizar a tarefa!", Toast.LENGTH_SHORT)
                .show()
            return
        }

        //se n tiver selecionado tarefa
        if (std == null) {
            Toast.makeText(
                this,
                "Selecione uma tarefa primeiro!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        //Verificar se os campos estão vazios
        if (name.isEmpty()) {
            Toast.makeText(
                this,
                "O primeiro campo é obrigatório, preencha corretamente!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val updateStd = StudentModel(id = std!!.id, name = name, email = email)
        val status = sqliteHelper.updateStudent(updateStd)
        if (status > -1) {
            clearEditText()
            getStudent()
            Toast.makeText(this, "Tarefa atualizada com sucesso!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Erro: Não foi possível fazer a atualização!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun deleteStudent(id: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Você tem certeza que deseja excluir essa tarefa?")
        builder.setCancelable(true)
        builder.setPositiveButton("Sim") { dialog, _ ->
            sqliteHelper.deleteStudentById(id)
            adapter?.removeItemById(id)
            getStudent()
            dialog.dismiss()
            Toast.makeText(this, "Tarefa excluída com sucesso!", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Não") { dialog, _ ->
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.show()

        val positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE)
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.start_color))
        negativeButton.setTextColor(ContextCompat.getColor(this, R.color.start_color))
    }

    private fun clearEditText() {
        edName.setText("")
        edEmail.setText("")
        edName.requestFocus()
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StudentAdapter()
        recyclerView.adapter = adapter
    }

    private fun initView() {
        edName = findViewById(R.id.edName)
        edEmail = findViewById(R.id.edEmail)
        btnAdd = findViewById(R.id.btnAdd)
        btnView = findViewById(R.id.btnView)
        btnUpdate = findViewById(R.id.btnUpdate)
        imgv_carioca = findViewById(R.id.imgv_carioca)
        recyclerView = findViewById(R.id.recyclerView)
    }

    private fun fadeOutImage() {

        if (isFirstView) {
            return
        } else {
            isFirstView = true
            val fadeOut = AlphaAnimation(1f, 0f)
            fadeOut.duration = 300 // Tempo da animação em milissegundos
            fadeOut.fillAfter = true // Manter a imagem invisível após a animação

            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    // Tornar a imagem invisível após a animação
                    imgv_carioca.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })

            imgv_carioca.startAnimation(fadeOut)
        }

    }

    private fun fadeInImage() {
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 1200 // Tempo da animação em milissegundos

        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {}

            override fun onAnimationRepeat(animation: Animation) {}
        })

        imgv_carioca.startAnimation(fadeIn)
    }
}