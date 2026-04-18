import { useEffect,useState } from "react";
import { fetchUsers,searchUsersByName,fetchUsersById,updateUser } from "../api/client";
import { Link } from "react-router-dom";

export function AdminUserPage(){
    const [users,setUsers]=useState<any[]>([]) 
    const [search,setSearch]=useState('')
    const [editingUser,setEditingUser]=useState<any|null>(null)
    const [loading,setLoading]=useState(true)

    //se carga los usuarios desde el backend
    async function load() {
        const data=await fetchUsers()
        setUsers(data)
        setLoading(false)
    }
    // se ejecuta una sola vez al montar el componentee
    useEffect(()=>{
        load()
    },[])

    //si esta vacio,  recarga todo
    async function handleSearch() {
        if (!search.trim())
            return load()

        try{ //para buscar por id
            const users=await fetchUsersById(search)
            setUsers([users]) //se guarda como array
            return
        }catch{}
        // si no se puede por el id, busca por el nombre
        const result=await searchUsersByName(search)
        setUsers(result)
    }
    // en el backend se actualiza el usuario
    async function handleUpdate() {
        if (!editingUser)
            return

        await updateUser(editingUser.id,editingUser)
        setEditingUser(null) //cerrar el formulario
        load() //se recarga la lista actualizada
    }

    return(
        <>
            <h2>Usuarios</h2>

            {/* buscador*/}
            <section className="card" style={{marginBottom: '1rem'}}>
                <input 
                    placeholder="Buscar por nombre o ID"
                    value={search}
                    onChange={(e)=>setSearch(e.target.value)}
                    style={{ marginRight: '0.5rem', padding:'0.6rem',width:'260px'}}
                />
                <button className="btn" onClick={handleSearch}>Buscar</button>
            </section>

            {/* formulario para la edicion*/}
            {editingUser && (
                <section className="card" style={{marginBottom:'1rem'}}>
                    <h3>Editar Usuario</h3>

                    <div className="form">
                        <label>
                            Nombre 
                            <input
                            value={editingUser.fullName}
                            onChange={(e)=>
                                setEditingUser({...editingUser, fullName:e.target.value})
                            }
                        />
                        </label>

                        <label>
                            Email 
                            <input
                            value={editingUser.email}
                            onChange={(e)=>
                                setEditingUser({...editingUser,email:e.target.value})
                            }
                        />
                        </label>

                        <div className="admin-form-actions">
                            <button className="btn" onClick={handleUpdate}>Guardar</button>
                            <button className="btn btn-secondary"
                            onClick={()=>setEditingUser(null)}>Cancelar</button>
                        </div>
                    </div>
                </section>
            )}

            {/* tabla de usuarios*/}
            <section>
                <h3>Lista de usuarios ({users.length})</h3>

                {loading ? (
                    <p>Cargando...</p>
                ):(
                    <div className="admin-table-wrap">
                        <table className="admin-table">
                            <thead>
                                <tr>
                                    <th>Nombre</th>
                                    <th>Email</th>
                                    <th>Rol</th>
                                    <th/>
                                </tr>
                            </thead>

                            <tbody>
                                {users.map((u)=>(
                                    <tr key={u.id}>
                                        <td>{u.fullName}</td>
                                        <td>{u.email}</td>
                                        <td>{u.role}</td>
                                        <td className="admin-actions">
                                            <button
                                            className="btn btn-sm"
                                            onClick={()=>setEditingUser(u)}>Editar</button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>

            <Link to="/admin">
            <button className="btn btn-secondary" style={{marginBottom:'1rem'}}>
                Volver</button></Link>
        </>
    )
}