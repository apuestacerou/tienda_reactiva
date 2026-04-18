import { useEffect,useState } from "react";
import { fetchUsers,serchUsersByName,fetchUsersById,updateUser } from "../api/client";

export function AdminUserPage(){
    const [users,setUsers]=useState<any[]>([])
    const [search,setSearch]=useState('')
    const [editingUser,setEditingUser]=useState<any|null>(null)

    async function load() {
        const data=await fetchUsers()
        setUsers(data)
    }
    useEffect(()=>{
        load()
    },[])

    async function handleSearch() {
        if (!search.trim())
            return load()

        try{
            const users=await fetchUsersById(search)
            setUsers([users])
            return
        }catch{}

        const result=await serchUsersByName(search)
        setUsers(result)
    }

    async function handleUpdate() {
        if (!editingUser)
            return

        await updateUser(editingUser.id,editingUser)
        setEditingUser(null)
        load()
    }

    return(
        <>
            <h2>Usuarios</h2>

            <input
            placeholder="Buscar por nombre o ID"
            value={search}
            onChange={(e)=>setSearch(e.target.value)}
            />
            <button onClick={handleSearch}>Buscar</button>

            <ul>
                {users.map((u)=>(
                    <li key={u.id}>
                        {u.fullName}-{u.email}
                        <button onClick={()=>setEditingUser(u)}>Editar</button>
                    </li>
                ))}
            </ul>

            {editingUser && (
                <div>
                    <h3>Editar usuario</h3>

                    <input
                    value={editingUser.fullName}
                    onChange={(e)=>
                        setEditingUser({...editingUser,fullName:e.target.value})
                    }
                    />

                    <input 
                    value={editingUser.email}
                    onChange={(e)=>
                        setEditingUser({...editingUser,email:e.target.value})
                    }
                    />

                    <button onClick={handleUpdate}>Guardar</button>
                </div>
            )}
        </>
    )
}