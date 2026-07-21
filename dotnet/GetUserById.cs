using System;
using System.IO;
using System.Text.Json;

/// <summary>
/// SDK demo method: getUserById (.NET / C# implementation).
///
/// Reads user records from the shared flat file (data/users.json) and
/// returns the username for a given user ID. Uses the built-in
/// System.Text.Json - no external packages required.
///
/// Usage:
///   dotnet run <id>     // look up a specific ID
///   dotnet run          // runs a built-in demo lookup (ID 101)
/// </summary>
public static class GetUserById
{
    private const int DemoId = 101;

    /// <summary>Candidate locations for the shared flat file.</summary>
    private static readonly string[] DataFileCandidates =
    {
        Path.Combine("..", "data", "users.json"),  // run from the dotnet/ folder
        Path.Combine("data", "users.json"),        // run from the repo root
        // When published/run via 'dotnet run', resolve relative to the project folder too:
        Path.Combine(AppContext.BaseDirectory, "..", "..", "..", "..", "data", "users.json")
    };

    /// <summary>
    /// Returns the username for the given user ID, or null if not found.
    /// </summary>
    public static string? Lookup(int userId)
    {
        string json = ReadDataFile();
        using JsonDocument doc = JsonDocument.Parse(json);
        foreach (JsonElement user in doc.RootElement.GetProperty("users").EnumerateArray())
        {
            if (user.GetProperty("id").GetInt32() == userId)
            {
                return user.GetProperty("username").GetString();
            }
        }
        return null;
    }

    private static string ReadDataFile()
    {
        foreach (string candidate in DataFileCandidates)
        {
            if (File.Exists(candidate))
            {
                return File.ReadAllText(candidate);
            }
        }
        throw new FileNotFoundException(
            "Could not find data/users.json - run from the repo root or the dotnet/ folder");
    }

    public static int Main(string[] args)
    {
        int userId;
        if (args.Length > 0)
        {
            if (!int.TryParse(args[0], out userId))
            {
                Console.WriteLine($"ERROR: '{args[0]}' is not a valid integer ID");
                return 2;
            }
        }
        else
        {
            Console.WriteLine($"No ID supplied - running demo lookup for ID {DemoId}");
            userId = DemoId;
        }

        try
        {
            string? username = Lookup(userId);
            if (username is null)
            {
                Console.WriteLine($"User with ID {userId} not found");
                return 1;
            }
            Console.WriteLine($"getUserById({userId}) -> {username}");
            return 0;
        }
        catch (FileNotFoundException e)
        {
            Console.WriteLine($"ERROR: {e.Message}");
            return 2;
        }
    }
}
